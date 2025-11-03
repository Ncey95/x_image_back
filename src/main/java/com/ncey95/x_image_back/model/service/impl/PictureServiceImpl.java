package com.ncey95.x_image_back.model.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ncey95.x_image_back.api.aliyunai.AliYunAiApi;
import com.ncey95.x_image_back.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.ncey95.x_image_back.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.ncey95.x_image_back.exception.BusinessException;
import com.ncey95.x_image_back.exception.ErrorCode;
import com.ncey95.x_image_back.exception.ThrowUtils;
import com.ncey95.x_image_back.manager.CosManager;
import com.ncey95.x_image_back.manager.upload.FilePictureUpload;
import com.ncey95.x_image_back.manager.upload.PictureUploadTemplate;
import com.ncey95.x_image_back.manager.upload.UrlPictureUpload;
import com.ncey95.x_image_back.model.dto.file.UploadPictureResult;
import com.ncey95.x_image_back.model.dto.picture.*;
import com.ncey95.x_image_back.model.enums.PictureReviewStatusEnum;
import com.ncey95.x_image_back.model.po.Picture;
import com.ncey95.x_image_back.model.mapper.PictureMapper;
import com.ncey95.x_image_back.model.po.Space;
import com.ncey95.x_image_back.model.po.User;
import com.ncey95.x_image_back.model.service.IPictureService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ncey95.x_image_back.model.service.ISpaceService;
import com.ncey95.x_image_back.model.service.UserService;
import com.ncey95.x_image_back.model.vo.PictureVO;
import com.ncey95.x_image_back.model.vo.UserVO;
import com.ncey95.x_image_back.utils.ColorSimilarUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 图片 服务实现类
 * </p>
 *
 * @author Ncey95
 * @since 2025-10-21
 */
@Service
@Slf4j
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements IPictureService {

    /*@Resource
    private FileManager fileManager;*/

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private UserService userService;

    @Resource
    private ISpaceService spaceService;

    @Resource
    private CosManager cosManager;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private AliYunAiApi aliYunAiApi;



    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // inputSource 输入源 可以是 MultipartFile 或 String的url 类型
        // PictureUploadRequest 图片上传请求参数
        // User 登录用户

        // 校验图片是否为空
        if (inputSource == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片为空");
        }

        // 登录用户为空 抛出未登录异常
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 校验空间是否为空
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 校验是不是空间的创建者 仅仅空间管理员可上传
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            }
            // 校验额度
            if (space.getTotalCount() >= space.getMaxCount()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间条数不足");
            }
            if (space.getTotalSize() >= space.getMaxSize()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间大小不足");
            }
        }
        // 图片 id
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }//如果图片 id 不为空 则获得ID给 pictureId

        //如果是更新操作 则校验图片是否存在 是否管理员审核
        if (pictureId != null) {
            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

            // 判断权限 仅本人和管理员可更新
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
            // 校验空间是否一致
            // 没传也复用
            if(spaceId == null){
                // 有传 则校验是否一致
                if(oldPicture.getSpaceId() != null){
                    spaceId= oldPicture.getSpaceId();
                }
            }else {
                // 空间为空 则复用旧空间
                if(ObjUtil.notEqual(spaceId, oldPicture.getSpaceId())){
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间不一致");
                }
            }
        }

        // 图片前缀 公共目录下 用户 id 目录
        // 按照用户id划分目录

        String uploadPathPrefix ;
        if(spaceId == null){
            uploadPathPrefix = String.format("public/%s", loginUser.getId());
        }else{
            uploadPathPrefix = String.format("space/%s", spaceId);
        }

        //根据InputSource的类型 区分上传方式
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        // 文件上传到对象存储 uploadPictureResult 包含图片 url 等信息
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);
        //构造要入库的图片对象

        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl()); // 图片 url
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl()); // 缩略图 url
        //支持外层传递图片名称 否则使用默认名称
        String picName = uploadPictureResult.getPicName();
        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picName = pictureUploadRequest.getPicName();
        }
        picture.setName(picName);
        // 空间id
        picture.setSpaceId(spaceId);
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        // 图片主色调
        picture.setPicColor(uploadPictureResult.getPicColor());

        picture.setUserId(loginUser.getId());


        //补充审核参数
        this.fillReviewParams(picture, loginUser);

        // 如果pictureId != null 不为空 则更新 否则新增
        if (pictureId != null) {

            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }

        // 更新空间的使用额度
        Long finalSpaceId = spaceId;
        transactionTemplate.execute(status -> {
            // 插入数据操作数据库
            boolean result = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败");
            if (finalSpaceId != null) {
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("totalSize = totalSize + " + picture.getPicSize())
                        .setSql("totalCount = totalCount + 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
            }
            return picture;
        });

        return PictureVO.objToVo(picture);
    }

    // 构造查询条件
    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        // QueryWrapper 用于构造查询条件 可以添加查询条件 排序等
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }

        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        // 空间id
        Long spaceId = pictureQueryRequest.getSpaceId();
        // 是否查询空间下的图片
        boolean nullSpaceId = pictureQueryRequest.isNullSpaceId();
        // 上传时间范围
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();


        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();

        // 如果搜索文本不为空 则添加搜索条件 名称或简介中包含搜索文本
        if (StrUtil.isNotBlank(searchText)) {
            // 搜索文本查询 名称或简介中包含搜索文本
            queryWrapper.and(qw -> qw.like("name", searchText)
                    .or()
                    .like("introduction", searchText)
            );
        }

        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.isNull(nullSpaceId, "spaceId");
        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
        queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);

        // 上面这一大块是根据查询参数构造查询条件

        // 标签查询 标签列表中包含所有标签
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序字段
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }


    // 获取图片VO 包含用户信息
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        //对象转封装类 vo
        PictureVO pictureVO = PictureVO.objToVo(picture);

        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    //分页获取图片封装：
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }

        //对象列表 转vo类列表
        List<PictureVO> pictureVOList = pictureList.stream().map(PictureVO::objToVo).collect(Collectors.toList());

        // 从图片列表中提取所有用户ID
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        //根据用户ID查询用户信息
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 为每个图片VO设置用户信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    //编写图片数据校验方法，用于更新和修改图片时进行判断：
    @Override
    public void validPicture(Picture picture) {
        // 校验对象是否为空
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);

        //从图片中获取信息
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();

        //校验id是否为空
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        //校验参数
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        if (id == null || reviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //判断是否图片存在
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        // 校验图片审核状态是否重复
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿重复审核");
        }

        // 数据库操作 更新
        Picture updatePicture = new Picture();
        BeanUtils.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(LocalDateTime.parse(DateUtil.now()));
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    //填充审核参数
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {

            //管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewerId(loginUser.getId());
            picture.setReviewMessage("管理员自动过审");
            picture.setReviewTime(LocalDateTime.now());
        } else {

            //不是管理员 则设置为待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    // 批量抓取图片 通过url
    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        // 校验参数
        String searchText = pictureUploadByBatchRequest.getSearchText();
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(StrUtil.isBlank(searchText), ErrorCode.PARAMS_ERROR, "搜索文本不能为空");
        ThrowUtils.throwIf(count > 30, ErrorCode.PARAMS_ERROR, "数量不能超过30");
        // 校验图片名称前缀 默认等于搜索文本
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        // 抓取内容
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        // 解析内容
        Element div = document.getElementsByClass("dgControl").first();   // 获取图片容器
        if (ObjUtil.isEmpty(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "解析图片容器失败");
        }
        // 获取所有图片元素
        Elements imgElementList = div.select("img.mimg");
        int uploadCount = 0;  // 上传成功的图片数量计数器
        // 便利元素 依次上传图片
        for (Element imgElement : imgElementList) {
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过: {}", fileUrl);
                continue;
            }
            // 处理图片的地址,防止转义或者对象存储的问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }

            // 上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setFileUrl(fileUrl);
            // 处理图片名称
            if (StrUtil.isNotBlank(namePrefix)) {
                // 处理图片名称 防止重复
                pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));
            }
            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                log.info("图片上传成功, id = {}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }

    // 异步清除图片文件
    @Async // 异步
    @Override
    public void clearPictureFile(Picture oldPicture) {
        // 判断该图片是否被多条记录使用
        String pictureUrl = oldPicture.getUrl();
        long count = this.lambdaQuery()
                .eq(Picture::getUrl, pictureUrl)
                .count();

        if (count > 1) {
            return;
        }
        // 删除cos存储桶中的图片文件
        cosManager.deleteObject(oldPicture.getUrl());
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            // 删除cos存储桶中的缩略图文件
            cosManager.deleteObject(thumbnailUrl);
        }
    }

    // 校验图片是否有操作权限
    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        if (spaceId == null) {
            // 公共图库仅 本人或管理员可以操作
            if (!picture.getUserId().equals(loginUser.getId()) && userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        } else {
            // 私有图库 仅 空间管理员可以操作
            if (!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
    }

    @Override
    public void deletePicture(long pictureId, User loginUser) {

        ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 判断是否存在
        Picture oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人和管理员可删除
        // 校验图片是否有操作权限
        // 以改为使用注解校验
        //checkPictureAuth(loginUser, oldPicture);
        // 开启事务
        transactionTemplate.execute(status -> {
            // 操作数据库
            boolean result = this.removeById(pictureId);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            // 更新空间使用额度,释放资源
            Long spaceId = oldPicture.getSpaceId();
            if (spaceId != null) {
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, spaceId)
                        .setSql("totalSize = totalSize - " + oldPicture.getPicSize())
                        .setSql("totalCount = totalCount - 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
            }
            return true;
        });
        // 异步清理文件
        this.clearPictureFile(oldPicture);
    }

    // 更新图片
    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {

        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);

        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));

        picture.setEditTime(new Date());

        this.validPicture(picture);

        long id = pictureEditRequest.getId();
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 已改为使用注解校验权限
        //checkPictureAuth(loginUser, oldPicture);

        this.fillReviewParams(picture, loginUser);

        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    // 按颜色搜索图片
    /**
     * 根据颜色搜索图片
     * 根据指定的空间ID和目标颜色，搜索该空间内颜色相似的图片并按相似度排序
     * @param spaceId 空间ID
     * @param picColor 目标颜色（十六进制格式，如：#FF0000 或 0xFF0000）
     * @param loginUser 登录用户信息
     * @return 按颜色相似度排序的图片VO列表，最多返回12张图片
     * @throws BusinessException 当参数错误、无权限或空间不存在时抛出
     */
    @Override
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
        // 参数校验：空间ID和颜色不能为空
        ThrowUtils.throwIf(spaceId == null || StrUtil.isBlank(picColor), ErrorCode.PARAMS_ERROR);
        // 权限校验：登录用户不能为空
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        // 查询空间信息，验证空间是否存在
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        // 权限校验：确保当前用户是空间的所有者
        if (!loginUser.getId().equals(space.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
        }

        // 查询指定空间内所有有颜色信息的图片
        List<Picture> pictureList = this.lambdaQuery()
                .eq(Picture::getSpaceId, spaceId) // 条件：空间ID匹配
                .isNotNull(Picture::getPicColor) // 条件：图片颜色信息不为空
                .list();

        // 如果没有找到图片，返回空列表
        if (CollUtil.isEmpty(pictureList)) {
            return Collections.emptyList();
        }

        // 将十六进制颜色字符串转换为Color对象
        Color targetColor = Color.decode(picColor);

        // 对图片列表按颜色相似度进行排序，并限制返回最多12张图片
        List<Picture> sortedPictures = pictureList.stream()
                .sorted(Comparator.comparingDouble(picture -> {
                    // 获取图片的十六进制颜色
                    String hexColor = picture.getPicColor();

                    // 对于没有颜色信息的图片，将其相似度设为最低（排到最后）
                    if (StrUtil.isBlank(hexColor)) {
                        return Double.MAX_VALUE;
                    }
                    // 将图片的十六进制颜色转换为Color对象
                    Color pictureColor = Color.decode(hexColor);

                    // 计算图片颜色与目标颜色的相似度，并取负值进行降序排序（相似度高的排前面）
                    // 使用负号是因为默认是升序排序，负号使其变为降序
                    return -ColorSimilarUtils.calculateSimilarity(targetColor, pictureColor);
                }))
                // 限制返回最多12张图片
                .limit(12)
                // 收集排序后的结果
                .collect(Collectors.toList());

        // 将排序后的Picture实体列表转换为PictureVO列表并返回
        return sortedPictures.stream()
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());
    }


    /**
     * 批量编辑图片信息
     * 支持批量更新图片的分类和标签信息，操作在事务中进行，确保数据一致性
     * @param pictureEditByBatchRequest 批量编辑请求对象，包含图片ID列表、空间ID、分类和标签信息
     * @param loginUser 登录用户信息
     * @throws BusinessException 当参数错误、无权限、空间不存在或操作失败时抛出
     */
    @Transactional(rollbackFor = Exception.class) // 事务注解，遇到任何异常时回滚
    @Override
    public void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {
        // 从请求对象中提取参数
        List<Long> pictureIdList = pictureEditByBatchRequest.getPictureIdList(); // 需要编辑的图片ID列表
        Long spaceId = pictureEditByBatchRequest.getSpaceId(); // 空间ID
        String category = pictureEditByBatchRequest.getCategory(); // 要更新的分类（可为空）
        List<String> tags = pictureEditByBatchRequest.getTags(); // 要更新的标签列表（可为空）

        // 参数校验：空间ID和图片ID列表不能为空
        ThrowUtils.throwIf(spaceId == null || CollUtil.isEmpty(pictureIdList), ErrorCode.PARAMS_ERROR);
        // 权限校验：登录用户不能为空
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        // 查询空间信息，验证空间是否存在
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        // 权限校验：确保当前用户是空间的所有者
        if (!loginUser.getId().equals(space.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
        }

        // 查询指定空间中、指定ID列表的图片（仅查询必要的ID和空间ID字段）
        List<Picture> pictureList = this.lambdaQuery()
                .select(Picture::getId, Picture::getSpaceId) // 只查询必要字段，提高性能
                .eq(Picture::getSpaceId, spaceId) // 条件：空间ID匹配
                .in(Picture::getId, pictureIdList) // 条件：图片ID在指定列表中
                .list();

        // 如果没有找到符合条件的图片，直接返回
        if (pictureList.isEmpty()) {
            return;
        }

        // 遍历图片列表，更新分类和标签信息（仅当请求中提供了相应信息时才更新）
        pictureList.forEach(picture -> {
            // 如果提供了分类信息，则更新图片分类
            if (StrUtil.isNotBlank(category)) {
                picture.setCategory(category);
            }
            // 如果提供了标签列表，则更新图片标签（将标签列表转换为JSON字符串）
            if (CollUtil.isNotEmpty(tags)) {
                picture.setTags(JSONUtil.toJsonStr(tags));
            }
        });
        // 批量重命名
        String nameRule = pictureEditByBatchRequest.getNameRule();
        fillPictureWithNameRule(pictureList, nameRule);

        // 批量更新图片信息
        boolean result = this.updateBatchById(pictureList);
        // 如果更新失败，抛出操作错误异常
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    // 批量重命名图片 私有方法
    private void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
        if (CollUtil.isEmpty(pictureList) || StrUtil.isBlank(nameRule)) {
            return;
        }
        long count = 1;
        try {
            for (Picture picture : pictureList) {
                String pictureName = nameRule.replaceAll("\\{序号}", String.valueOf(count++));
                picture.setName(pictureName);
            }
        } catch (Exception e) {
            log.error("名称解析错误", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "名称解析错误");
        }
    }

    // 创建AI扩图出图任务
    @Override
    public CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser) {
        // 校验图片ID是否存在
        Long pictureId = createPictureOutPaintingTaskRequest.getPictureId();
        Picture picture = Optional.ofNullable(this.getById(pictureId))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ERROR));
        // 校验用户是否有操作该图片的权限
        // 已改为使用注解校验权限
        //checkPictureAuth(loginUser, picture);
        // 构造扩图任务 参数
        CreateOutPaintingTaskRequest taskRequest = new CreateOutPaintingTaskRequest();
        CreateOutPaintingTaskRequest.Input input = new CreateOutPaintingTaskRequest.Input();
        input.setImageUrl(picture.getUrl());
        taskRequest.setInput(input);
        BeanUtil.copyProperties(createPictureOutPaintingTaskRequest, taskRequest);

        return aliYunAiApi.createOutPaintingTask(taskRequest);
    }

}
