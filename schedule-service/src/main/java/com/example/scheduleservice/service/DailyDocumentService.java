package com.example.scheduleservice.service;

import com.example.common.exception.BusinessException;
import com.example.common.result.PageResult;
import com.example.common.result.ResultCode;
import com.example.scheduleservice.entity.DailyDocument;
import com.example.scheduleservice.mapper.DailyDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日文档业务服务
 *
 * <p>提供每日文档的完整CRUD操作，以及按日期查询、按日期区间列表等业务功能。
 * 每日文档用于记录用户每天的日记、工作日志等内容。</p>
 *
 * <p><b>主要功能：</b></p>
 * <ul>
 *   <li>文档创建、查询、更新、删除</li>
 *   <li>按指定日期查询文档</li>
 *   <li>按日期区间查询文档列表</li>
 *   <li>分页查询支持</li>
 *   <li>支持软删除到垃圾箱</li>
 * </ul>
 *
 * <p><b>使用场景：</b></p>
 * <ul>
 *   <li>个人日记记录</li>
 *   <li>工作日志管理</li>
 *   <li>每日总结回顾</li>
 * </ul>
 *
 * @author Schedule Service
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DailyDocumentService {

    private final DailyDocumentMapper dailyDocumentMapper;
    private final TrashService trashService;

    /**
     * 创建每日文档
     *
     * <p>创建一个新的每日文档记录，按日期组织内容。</p>
     *
     * @param doc 文档实体对象
     *             <ul>
     *               <li>docDate: 文档日期（必填）</li>
     *               <li>title: 文档标题（可选）</li>
     *               <li>content: 文档内容（可选）</li>
     *             </ul>
     * @return 创建成功的文档对象
     * @throws BusinessException 如果创建失败
     */
    public DailyDocument create(DailyDocument doc) {
        // 验证必填字段
        if (doc.getDocDate() == null) {
            log.warn("❌ 创建每日文档失败：文档日期为空");
            throw new BusinessException(ResultCode.PARAM_INVALID.getCode(), "文档日期不能为空");
        }

        // 检查当日文档是否已存在
        DailyDocument existing = dailyDocumentMapper.selectByDocDate(doc.getDocDate());
        if (existing != null) {
            log.warn("❌ 创建每日文档失败：当日文档已存在 - 日期: {}", doc.getDocDate());
            throw new BusinessException(ResultCode.DUPLICATE_KEY.getCode(), "该日期的文档已存在，请使用更新功能");
        }

        // 保存文档
        dailyDocumentMapper.insert(doc);
        log.info("✅ 每日文档创建成功 - ID: {}, 日期: {}", doc.getId(), doc.getDocDate());

        return doc;
    }

    /**
     * 根据ID查询文档
     *
     * @param id 文档ID
     * @return 文档对象，不存在返回null
     */
    public DailyDocument getById(Long id) {
        if (id == null) {
            log.warn("⚠️ 查询文档失败：ID参数为空");
            return null;
        }

        DailyDocument doc = dailyDocumentMapper.selectById(id);
        if (doc == null) {
            log.debug("🔍 查询文档详情 - ID: {} 未找到", id);
        } else {
            log.debug("🔍 查询文档详情 - ID: {}, 日期: {}", id, doc.getDocDate());
        }

        return doc;
    }

    /**
     * 根据日期查询文档
     *
     * <p>获取指定日期的文档，如果不存在则返回null。</p>
     *
     * @param date 文档日期
     * @return 该日期的文档，不存在返回null
     *
     * @example
     * <pre>
     * DailyDocument doc = service.getByDocDate(LocalDate.of(2024, 1, 15));
     * </pre>
     */
    public DailyDocument getByDocDate(LocalDate date) {
        if (date == null) {
            log.warn("⚠️ 按日期查询失败：日期参数为空");
            return null;
        }

        DailyDocument doc = dailyDocumentMapper.selectByDocDate(date);

        if (doc == null) {
            log.debug("📄 按日期查询文档 - 日期: {} 未找到文档", date);
        } else {
            log.debug("📄 按日期查询文档 - 日期: {}, 标题: {}", date, doc.getTitle());
        }

        return doc;
    }

    /**
     * 按日期区间查询文档（分页）
     *
     * <p>获取指定日期范围内的所有文档，支持分页返回。</p>
     *
     * @param from 起始日期
     * @param to   结束日期
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @return 分页结果
     */
    public PageResult<DailyDocument> listByDateRangePaged(LocalDate from, LocalDate to, int page, int size) {
        // 参数验证
        if (from == null || to == null) {
            log.warn("❌ 日期区间查询失败：起始日期或结束日期为空");
            throw new BusinessException(ResultCode.PARAM_INVALID.getCode(), "起始日期和结束日期不能为空");
        }

        if (from.isAfter(to)) {
            log.warn("❌ 日期区间查询失败：起始日期 {} 晚于结束日期 {}", from, to);
            throw new BusinessException(ResultCode.PARAM_INVALID.getCode(), "起始日期不能晚于结束日期");
        }

        // 查询总数
        long total = dailyDocumentMapper.countByDateRange(from, to);
        log.info("📚 日期区间文档查询 - 从 {} 到 {} 共 {} 条", from, to, total);

        // 计算分页参数
        int offset = (page - 1) * size;
        List<DailyDocument> docs = dailyDocumentMapper.selectByDateRangePaged(from, to, offset, size);

        return new PageResult<>(docs, total, page, size);
    }

    /**
     * 更新文档
     *
     * <p>根据ID更新文档的标题和内容。</p>
     *
     * @param id  文档ID
     * @param doc 新的文档数据
     * @return 更新后的文档
     * @throws BusinessException 如果文档不存在
     */
    public DailyDocument update(Long id, DailyDocument doc) {
        // 参数验证
        if (id == null) {
            log.warn("❌ 更新文档失败：文档ID为空");
            throw new BusinessException(ResultCode.PARAM_INVALID.getCode(), "文档ID不能为空");
        }

        // 查询原文档
        DailyDocument existing = dailyDocumentMapper.selectById(id);
        if (existing == null) {
            log.warn("❌ 更新文档失败：文档不存在 - ID: {}", id);
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "文档不存在");
        }

        log.debug("✏️ 开始更新文档 - ID: {}", id);

        // 更新非空字段
        if (doc.getTitle() != null) {
            existing.setTitle(doc.getTitle());
        }
        if (doc.getContent() != null) {
            existing.setContent(doc.getContent());
        }

        // 保存更新
        dailyDocumentMapper.updateById(existing);
        log.info("✅ 文档更新成功 - ID: {}, 日期: {}", id, existing.getDocDate());

        return existing;
    }

    /**
     * 移动文档到垃圾箱
     *
     * <p>将指定文档移动到垃圾箱，支持30天内恢复。</p>
     *
     * @param id     文档ID
     * @param userId 用户ID
     * @throws BusinessException 如果文档不存在
     *
     * @see TrashService#moveToTrash
     */
    public void deleteById(Long id, Long userId) {
        log.debug("🗑️ 移动文档到垃圾箱 - ID: {}", id);

        // 参数验证
        if (id == null) {
            log.warn("❌ 删除文档失败：文档ID为空");
            throw new BusinessException(ResultCode.PARAM_INVALID.getCode(), "文档ID不能为空");
        }

        if (userId == null) {
            log.warn("❌ 删除文档失败：用户ID为空");
            throw new BusinessException(ResultCode.PARAM_INVALID.getCode(), "用户ID不能为空");
        }

        // 查询要删除的文档
        DailyDocument existing = dailyDocumentMapper.selectById(id);
        if (existing == null) {
            log.warn("🗑️ 删除文档失败：文档不存在 - ID: {}", id);
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "文档不存在");
        }

        // 保存原标题
        String title = existing.getTitle();
        LocalDate date = existing.getDocDate();

        // 移动到垃圾箱
        trashService.moveToTrash(
                userId,
                "document",
                id,
                title,
                existing.getContent(),
                existing
        );

        // 物理删除文档
        dailyDocumentMapper.deleteById(id);

        log.info("✅ 文档已移动到垃圾箱 - ID: {}, 日期: {}, 保留期: 30天", id, date);
    }
}
