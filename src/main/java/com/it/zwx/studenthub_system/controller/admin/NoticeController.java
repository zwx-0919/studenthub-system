package com.it.zwx.studenthub_system.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.zwx.studenthub_system.constant.MessageConstant;
import com.it.zwx.studenthub_system.pojo.dto.NoticeDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.NoticePageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Notice;
import com.it.zwx.studenthub_system.pojo.entity.Result;
import com.it.zwx.studenthub_system.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notice")
@Tag(name = "管理员公告管理")
@CrossOrigin
@Slf4j
public class NoticeController {

    @Autowired
    private NoticeService noticeService;


    /**
     * 1. 查看公告列表,条件查询：分页，标题，状态
    **/
    @GetMapping("/notice")
    @Operation(summary = "条件查询公告列表")
    public Result<Page<Notice>> listNotice(@ModelAttribute NoticePageDTO dto) {
        log.info("查看公告列表");
        Page<Notice> page = noticeService.listNotice(dto);
        return Result.success(page);
    }

    @GetMapping("/detail/{id}")
    @Operation(summary = "公告详情（浏览量+1）")
    public Result<Notice> detail(@PathVariable Integer id) {
        return Result.success(noticeService.detailAndIncrView(id));
    }

    // 2. 新增公告（POST）
    @PostMapping("/add")
    @Operation(summary = "新增公告")
    public Result addNotice(@RequestBody NoticeDTO dto) {
        log.info("新增公告");
        try {
            noticeService.addNotice(dto);
            return Result.success(MessageConstant.ADD_SUCCESS);
        } catch (Exception e) {
            return Result.error(MessageConstant.OPERATE_FAILED);
        }
        }
    // 3. 全量更新公告（PUT）
    @PutMapping("/notice/{id}")
    @Operation(summary = "更新公告")
    public Result updateNotice(@PathVariable Integer id, @RequestBody NoticeDTO dto) {
    log.info("全量更新公告");
    try {
        noticeService.updateNotice(id,dto);
        return Result.success(MessageConstant.UPDATE_SUCCESS);
    } catch (Exception e) {
        log.error("操作失败",e);
        return Result.error(MessageConstant.OPERATE_FAILED);
    }
    }

    // 4. 仅修改公告状态（PATCH）
    @PatchMapping("/notice/{id}")
    @Operation(summary = "修改公告状态")
    public Result updateNoticeStatus(@PathVariable Integer id) {
        log.info("仅修改公告状态");
        try {
            noticeService.updateNoticeStatus(id);
            return Result.success(MessageConstant.UPDATE_SUCCESS);
        } catch (Exception e) {
            log.error("操作失败",e);
            return Result.error(MessageConstant.OPERATE_FAILED);
        }
    }

    // 5. 删除公告（DELETE）
    @DeleteMapping
    @Operation(summary = "批量删除公告")
    public Result deleteNotice(@RequestBody List<Integer> ids) {
        log.info("删除公告");
        try {
            noticeService.deleteNotice(ids);
            return Result.success(MessageConstant.DELETE_SUCCESS);
        } catch (Exception e) {
            log.error("操作失败",e);
            return Result.error(MessageConstant.OPERATE_FAILED);
        }
    }
}
