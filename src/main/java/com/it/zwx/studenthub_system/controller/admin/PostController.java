package com.it.zwx.studenthub_system.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.zwx.studenthub_system.constant.MessageConstant;
import com.it.zwx.studenthub_system.pojo.dto.extend.PostPageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Post;
import com.it.zwx.studenthub_system.pojo.entity.Result;
import com.it.zwx.studenthub_system.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/post")
@Tag(name = "管理员帖子管理")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/page")
    @Operation(summary = "帖子分页查询")
    public Result<Page<Post>> page(@ModelAttribute PostPageDTO dto) {
        return Result.success(postService.adminPage(dto));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "修改帖子状态（下架/发布）")
    public Result updateStatus(@PathVariable Integer id, @RequestParam Integer status) {
        postService.updateStatus(id, status);
        return Result.success(MessageConstant.UPDATE_SUCCESS);
    }

    @DeleteMapping
    @Operation(summary = "批量删除帖子")
    public Result delete(@RequestBody List<Integer> ids) {
        postService.deleteByIds(ids);
        return Result.success(MessageConstant.DELETE_SUCCESS);
    }
}
