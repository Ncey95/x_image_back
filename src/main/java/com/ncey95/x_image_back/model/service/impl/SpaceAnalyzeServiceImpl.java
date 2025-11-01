package com.ncey95.x_image_back.model.service.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ncey95.x_image_back.exception.BusinessException;
import com.ncey95.x_image_back.exception.ErrorCode;
import com.ncey95.x_image_back.exception.ThrowUtils;
import com.ncey95.x_image_back.model.dto.analyze.*;
import com.ncey95.x_image_back.model.mapper.SpaceMapper;
import com.ncey95.x_image_back.model.po.Picture;
import com.ncey95.x_image_back.model.po.Space;
import com.ncey95.x_image_back.model.po.User;
import com.ncey95.x_image_back.model.service.IPictureService;
import com.ncey95.x_image_back.model.service.ISpaceService;
import com.ncey95.x_image_back.model.service.SpaceAnalyzeService;
import com.ncey95.x_image_back.model.service.UserService;
import com.ncey95.x_image_back.model.vo.analyze.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SpaceAnalyzeServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceAnalyzeService {

    @Resource
    private UserService userService;
    @Resource
    private ISpaceService spaceService;
    @Resource
    private IPictureService pictureService;



    /**
     * 检查空间分析操作的权限
     * 根据请求类型验证用户是否有权限执行空间分析操作
     *
     * @param spaceAnalyzeRequest 空间分析请求对象，包含查询类型和空间ID等信息
     * @param loginUser 当前登录用户信息
     * @throws BusinessException 当用户无权限或参数非法时抛出
     */
    private void checkSpaceAnalyzeAuth(SpaceAnalyzeRequest spaceAnalyzeRequest, User loginUser) {
        // 检查是否为查询所有空间或公共空间的请求
        if (spaceAnalyzeRequest.isQueryAll() || spaceAnalyzeRequest.isQueryPublic()) {
            // 只有管理员才能访问公共图库或查询所有空间
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "无权访问公共图库");
        } else {
            // 非公共查询，则需要验证具体空间的访问权限
            // 获取请求中的空间ID
            Long spaceId = spaceAnalyzeRequest.getSpaceId();
            // 校验空间ID参数合法性
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);
            // 根据ID查询空间信息
            Space space = spaceService.getById(spaceId);
            // 校验空间是否存在
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 调用空间服务检查用户对该空间的访问权限
            spaceService.checkSpaceAuth(loginUser, space);
        }
    }

    /**
     * 根据空间分析请求参数填充查询条件包装器
     * 该方法根据请求中的查询范围（全部、公开或特定空间）设置对应的查询条件
     * @param spaceAnalyzeRequest 空间分析请求对象，包含查询范围信息
     * @param queryWrapper 查询条件包装器，用于构建数据库查询条件
     * @throws BusinessException 当未指定有效查询范围时抛出参数错误异常
     */
    private static void fillAnalyzeQueryWrapper(SpaceAnalyzeRequest spaceAnalyzeRequest, QueryWrapper<Picture> queryWrapper) {
        // 如果请求查询全部图片，则不需要添加任何查询条件，直接返回
        if (spaceAnalyzeRequest.isQueryAll()) {
            return;
        }
        // 如果请求查询公开图片，则添加spaceId为null的条件（表示无所属空间的公开图片）
        if (spaceAnalyzeRequest.isQueryPublic()) {
            queryWrapper.isNull("spaceId");
            return;
        }
        // 获取请求中指定的空间ID
        Long spaceId = spaceAnalyzeRequest.getSpaceId();
        // 如果空间ID不为空，则添加spaceId等于指定值的条件
        if (spaceId != null) {
            queryWrapper.eq("spaceId", spaceId);
            return;
        }
        // 如果以上条件都不满足，说明未指定有效的查询范围，抛出业务异常
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "未指定查询范围");
    }

    /**
     * 获取空间使用情况分析数据
     * 根据请求参数获取全部空间、公开空间或特定空间的存储使用统计信息
     *
     * @param spaceUsageAnalyzeRequest 空间使用分析请求对象，包含查询范围信息
     * @param loginUser 当前登录用户信息
     * @return 空间使用情况分析响应对象，包含存储空间和图片数量的使用统计
     * @throws BusinessException 当参数无效、无权限访问或空间不存在时抛出异常
     */
    @Override
    public SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser) {
        // 参数校验：确保请求对象不为空
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);

        // 分支一：查询全部空间或公开空间的使用情况
        // 如果请求查询全部空间或公开空间，则执行以下逻辑
        if (spaceUsageAnalyzeRequest.isQueryAll() || spaceUsageAnalyzeRequest.isQueryPublic()) {
            // 权限校验：只有管理员才能查询全部或公开空间的使用情况
            boolean isAdmin = userService.isAdmin(loginUser);
            ThrowUtils.throwIf(!isAdmin, ErrorCode.NO_AUTH_ERROR, "无权访问空间");

            // 构建查询条件，只查询图片大小字段
            QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("picSize");

            // 如果不是查询全部空间，则添加spaceId为null的条件（表示公开图片）
            if (!spaceUsageAnalyzeRequest.isQueryAll()) {
                queryWrapper.isNull("spaceId");
            }

            // 执行查询，获取所有符合条件的图片大小数据
            List<Object> pictureObjList = pictureService.getBaseMapper().selectObjs(queryWrapper);

            // 计算已使用的总存储空间（字节），过滤并累加所有有效数据
            long usedSize = pictureObjList.stream().mapToLong(result -> result instanceof Long ? (Long) result : 0).sum();

            // 计算已使用的图片数量
            long usedCount = pictureObjList.size();

            // 创建响应对象并设置使用统计数据
            SpaceUsageAnalyzeResponse spaceUsageAnalyzeResponse = new SpaceUsageAnalyzeResponse();
            spaceUsageAnalyzeResponse.setUsedSize(usedSize);
            spaceUsageAnalyzeResponse.setUsedCount(usedCount);

            // 对于全部或公开空间，不设置最大限制和使用率（设置为null）
            spaceUsageAnalyzeResponse.setMaxSize(null);
            spaceUsageAnalyzeResponse.setSizeUsageRatio(null);
            spaceUsageAnalyzeResponse.setMaxCount(null);
            spaceUsageAnalyzeResponse.setCountUsageRatio(null);
            return spaceUsageAnalyzeResponse;
        } else {
            // 分支二：查询特定空间的使用情况

            // 获取请求中的空间ID并验证其有效性
            Long spaceId = spaceUsageAnalyzeRequest.getSpaceId();
            ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR);

            // 根据ID查询空间信息并验证空间是否存在
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");

            // 检查当前用户是否有权限访问该空间
            spaceService.checkSpaceAuth(loginUser, space);

            // 创建响应对象并设置空间的使用统计数据
            SpaceUsageAnalyzeResponse response = new SpaceUsageAnalyzeResponse();
            response.setUsedSize(space.getTotalSize());  // 已使用存储空间
            response.setMaxSize(space.getMaxSize());     // 最大允许存储空间

            // 计算存储空间使用率（保留两位小数）
            double sizeUsageRatio = NumberUtil.round(space.getTotalSize() * 100.0 / space.getMaxSize(), 2).doubleValue();
            response.setSizeUsageRatio(sizeUsageRatio);

            response.setUsedCount(space.getTotalCount());  // 已使用图片数量
            response.setMaxCount(space.getMaxCount());     // 最大允许图片数量

            // 计算图片数量使用率（保留两位小数）
            double countUsageRatio = NumberUtil.round(space.getTotalCount() * 100.0 / space.getMaxCount(), 2).doubleValue();
            response.setCountUsageRatio(countUsageRatio);

            return response;
        }
    }

    /**
     * 获取空间图片类别分析数据
     * 根据查询条件统计不同类别的图片数量和总大小
     *
     * @param spaceCategoryAnalyzeRequest 空间类别分析请求参数，包含查询范围和过滤条件
     * @param loginUser 当前登录用户信息，用于权限校验
     * @return 各类别的图片统计数据列表，每个元素包含类别名称、图片数量和总大小
     * @throws BusinessException 当参数错误、权限不足或其他业务逻辑异常时抛出
     */
    @Override
    public List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser) {
        // 参数校验：确保请求对象不为空
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);

        // 权限检查：验证用户是否有查询该空间分析数据的权限
        checkSpaceAnalyzeAuth(spaceCategoryAnalyzeRequest, loginUser);

        // 创建查询包装器，用于构建数据库查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();

        // 填充查询条件：根据请求参数设置合适的过滤条件（如空间ID、公开/私有等）
        fillAnalyzeQueryWrapper(spaceCategoryAnalyzeRequest, queryWrapper);

        // 设置查询的字段和分组条件
        // category: 图片类别
        // COUNT(*): 各类别图片数量
        // SUM(picSize): 各类别图片总大小
        queryWrapper.select("category AS category",
                        "COUNT(*) AS count",
                        "SUM(picSize) AS totalSize")
                .groupBy("category");

        // 执行查询并将结果转换为响应对象列表
        // 1. 执行SQL查询获取统计数据
        // 2. 将结果集转换为流进行处理
        // 3. 映射每一行结果到SpaceCategoryAnalyzeResponse对象
        // 4. 收集结果为List返回
        return pictureService.getBaseMapper().selectMaps(queryWrapper)
                .stream()
                .map(result -> {
                    // 获取类别名称，若为null则设为"未分类"
                    String category = result.get("category") != null ? result.get("category").toString() : "未分类";
                    // 获取图片数量
                    Long count = ((Number) result.get("count")).longValue();
                    // 获取图片总大小
                    Long totalSize = ((Number) result.get("totalSize")).longValue();
                    // 创建并返回类别分析响应对象
                    return new SpaceCategoryAnalyzeResponse(category, count, totalSize);
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取空间图片标签分析数据
     * 根据查询条件统计不同标签的使用频率（出现次数）
     *
     * @param spaceTagAnalyzeRequest 空间标签分析请求参数，包含查询范围和过滤条件
     * @param loginUser 当前登录用户信息，用于权限校验
     * @return 标签分析结果列表，每个元素包含标签名称和出现次数，按出现次数降序排列
     * @throws BusinessException 当参数错误、权限不足或其他业务逻辑异常时抛出
     */
    @Override
    public List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser) {
        // 参数校验：确保请求对象不为空
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);

        // 权限检查：验证用户是否有查询该空间分析数据的权限
        checkSpaceAnalyzeAuth(spaceTagAnalyzeRequest, loginUser);

        // 创建查询包装器，用于构建数据库查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        // 填充查询条件：根据请求参数设置合适的过滤条件（如空间ID、公开/私有等）
        fillAnalyzeQueryWrapper(spaceTagAnalyzeRequest, queryWrapper);

        // 设置查询仅返回tags字段
        queryWrapper.select("tags");
        // 执行查询并处理结果：
        // 1. 从数据库查询所有匹配图片的tags字段
        // 2. 过滤掉null值
        // 3. 将结果转换为字符串列表
        List<String> tagsJsonList = pictureService.getBaseMapper().selectObjs(queryWrapper)
                .stream()
                .filter(ObjUtil::isNotNull)
                .map(Object::toString)
                .collect(Collectors.toList());

        // 统计每个标签的出现次数：
        // 1. 将每个JSON格式的标签数组转换为字符串列表
        // 2. 使用flatMap 将多个列表合并为一个流
        // 3. 按标签分组并统计每组数量
        Map<String, Long> tagCountMap = tagsJsonList.stream()
                .flatMap(tagsJson -> JSONUtil.toList(tagsJson, String.class).stream())
                .collect(Collectors.groupingBy(tag -> tag, Collectors.counting()));

        // 转换为响应对象列表并排序：
        // 1. 将Map转换为Stream
        // 2. 按标签出现次数降序排序（注意e2.getValue()和e1.getValue()的顺序）
        // 3. 映射每个条目到SpaceTagAnalyzeResponse对象
        // 4. 收集结果为List返回
        return tagCountMap.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .map(entry -> new SpaceTagAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 获取空间图片大小分析数据
     * 根据查询条件统计不同大小范围的图片数量分布
     *
     * @param spaceSizeAnalyzeRequest 空间大小分析请求参数，包含查询范围和过滤条件
     * @param loginUser 当前登录用户信息，用于权限校验
     * @return 图片大小分布统计结果列表，每个元素包含大小范围和对应数量
     * @throws BusinessException 当参数错误、权限不足或其他业务逻辑异常时抛出
     */
    @Override
    public List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser) {
        // 参数校验：确保请求对象不为空
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);

        // 权限检查：验证用户是否有查询该空间分析数据的权限
        checkSpaceAnalyzeAuth(spaceSizeAnalyzeRequest, loginUser);

        // 创建查询包装器，用于构建数据库查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        // 填充查询条件：根据请求参数设置合适的过滤条件（如空间ID、公开/私有等）
        fillAnalyzeQueryWrapper(spaceSizeAnalyzeRequest, queryWrapper);

        // 设置查询仅返回picSize字段
        queryWrapper.select("picSize");
        // 执行查询并处理结果：
        // 1. 从数据库查询所有匹配图片的picSize字段
        // 2. 将结果转换为Long类型的列表
        List<Long> picSizes = pictureService.getBaseMapper().selectObjs(queryWrapper)
                .stream()
                .map(size -> ((Number) size).longValue())
                .collect(Collectors.toList());

        // 统计不同大小范围的图片数量
        // 使用LinkedHashMap保持插入顺序，确保响应结果中的大小范围顺序一致
        Map<String, Long> sizeRanges = new LinkedHashMap<>();
        // 统计小于100KB的图片数量
        sizeRanges.put("<100KB", picSizes.stream().filter(size -> size < 100 * 1024).count());
        // 统计100KB-500KB的图片数量
        sizeRanges.put("100KB-500KB", picSizes.stream().filter(size -> size >= 100 * 1024 && size < 500 * 1024).count());
        // 统计500KB-1MB的图片数量
        sizeRanges.put("500KB-1MB", picSizes.stream().filter(size -> size >= 500 * 1024 && size < 1 * 1024 * 1024).count());
        // 统计大于等于1MB的图片数量
        sizeRanges.put(">1MB", picSizes.stream().filter(size -> size >= 1 * 1024 * 1024).count());

        // 转换为响应对象列表：
        // 1. 将Map转换为Stream
        // 2. 映射每个条目到SpaceSizeAnalyzeResponse对象
        // 3. 收集结果为List返回
        return sizeRanges.entrySet().stream()
                .map(entry -> new SpaceSizeAnalyzeResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 获取空间用户图片上传趋势分析数据
     * 根据时间维度统计指定用户或空间内图片的上传数量趋势
     *
     * @param spaceUserAnalyzeRequest 空间用户分析请求参数，包含查询范围、用户ID和时间维度等信息
     * @param loginUser 当前登录用户信息，用于权限校验
     * @return 按时间周期统计的图片数量列表，每个元素包含时间周期和对应图片数量
     * @throws BusinessException 当参数错误、权限不足或其他业务逻辑异常时抛出
     */
    @Override
    public List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser) {
        // 参数校验：确保请求对象不为空
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);

        // 权限检查：验证用户是否有查询该空间分析数据的权限
        checkSpaceAnalyzeAuth(spaceUserAnalyzeRequest, loginUser);

        // 创建查询包装器，用于构建数据库查询条件
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        // 获取用户ID参数，如果存在则添加到查询条件中
        Long userId = spaceUserAnalyzeRequest.getUserId();
        queryWrapper.eq(ObjUtil.isNotNull(userId), "userId", userId);
        // 填充其他查询条件：根据请求参数设置合适的过滤条件（如空间ID、公开/私有等）
        fillAnalyzeQueryWrapper(spaceUserAnalyzeRequest, queryWrapper);

        // 获取时间维度参数（day/week/month），并根据不同维度设置相应的日期格式化方式
        String timeDimension = spaceUserAnalyzeRequest.getTimeDimension();
        switch (timeDimension) {
            case "day":
                // 按天统计：格式化日期为YYYY-MM-DD格式
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m-%d') AS period", "COUNT(*) AS count");
                break;
            case "week":
                // 按周统计：使用YEARWEEK函数获取年份+周数（格式：YYYYWW）
                queryWrapper.select("YEARWEEK(createTime) AS period", "COUNT(*) AS count");
                break;
            case "month":
                // 按月统计：格式化日期为YYYY-MM格式
                queryWrapper.select("DATE_FORMAT(createTime, '%Y-%m') AS period", "COUNT(*) AS count");
                break;
            default:
                // 不支持的时间维度，抛出参数错误异常
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的时间维度");
        }

        // 按时间周期分组，并按时间顺序升序排列
        queryWrapper.groupBy("period").orderByAsc("period");

        // 执行查询并处理结果：
        // 1. 执行SQL查询获取统计数据
        // 2. 将结果集转换为流进行处理
        // 3. 映射每一行结果到SpaceUserAnalyzeResponse对象
        // 4. 收集结果为List返回
        List<Map<String, Object>> queryResult = pictureService.getBaseMapper().selectMaps(queryWrapper);
        return queryResult.stream()
                .map(result -> {
                    // 获取时间周期字段值
                    String period = result.get("period").toString();
                    // 获取该时间周期的图片数量
                    Long count = ((Number) result.get("count")).longValue();
                    // 创建并返回用户分析响应对象
                    return new SpaceUserAnalyzeResponse(period, count);
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取空间使用量排行分析数据
     * 根据空间总使用量降序排列，返回指定数量的前N名空间
     *
     * @param spaceRankAnalyzeRequest 空间排行分析请求对象，包含查询参数和限制数量
     * @param loginUser 当前登录用户信息，用于权限校验
     * @return 空间列表，按总使用量降序排列，包含空间ID、空间名称、用户ID和总大小信息
     * @throws BusinessException 当参数错误或无权限时抛出异常
     */
    @Override
    public List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser) {
        // 校验请求参数是否为空
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);

        // 校验用户权限，只有管理员才能查看空间排行
        ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "无权查看空间排行");

        // 构建查询条件，选择需要的字段并按总使用量降序排序
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "spaceName", "userId", "totalSize")
                .orderByDesc("totalSize")
                .last("LIMIT " + spaceRankAnalyzeRequest.getTopN());

        // 执行查询并返回结果
        return spaceService.list(queryWrapper);
    }
}
