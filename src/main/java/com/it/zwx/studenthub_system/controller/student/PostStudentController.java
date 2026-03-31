package com.it.zwx.studenthub_system.controller.student;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.zwx.studenthub_system.pojo.dto.PostAddDTO;
import com.it.zwx.studenthub_system.pojo.dto.PostCommentAddDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.PostPageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Post;
import com.it.zwx.studenthub_system.pojo.entity.PostComment;
import com.it.zwx.studenthub_system.pojo.entity.Result;
import com.it.zwx.studenthub_system.pojo.vo.PostDetailVO;
import com.it.zwx.studenthub_system.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/student/post")
@Tag(name = "学生帖子")
@RequiredArgsConstructor
public class PostStudentController {

    private static final Set<String> ALLOWED_SUFFIX = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final Set<String> ALLOWED_CONTENT_TYPE = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L;

    private final PostService postService;

    @Value("${studenthub.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/add")
    @Operation(summary = "发布帖子")
    public Result<Post> add(@RequestBody @Validated PostAddDTO dto) {
        return Result.success(postService.add(dto));
    }

    @PostMapping("/upload-image")
    @Operation(summary = "上传帖子图片")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择图片文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error("图片大小不能超过 5MB");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPE.contains(contentType.toLowerCase(Locale.ROOT))) {
            return Result.error("图片格式不合法");
        }
        String originalName = file.getOriginalFilename();
        String suffix = "";
        if (StringUtils.hasText(originalName) && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        }
        if (!ALLOWED_SUFFIX.contains(suffix)) {
            return Result.error("仅支持 jpg/jpeg/png/webp/gif 图片");
        }
        File dir = new File(uploadDir);
        if (!dir.exists() && !dir.mkdirs()) {
            return Result.error("创建上传目录失败");
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + suffix;
        File target = new File(dir, fileName);
        file.transferTo(target);
        return Result.success("/uploads/" + fileName);
    }

    @GetMapping("/page")
    @Operation(summary = "帖子分页查询")
    public Result<Page<Post>> page(@ModelAttribute PostPageDTO dto) {
        return Result.success(postService.pagePublished(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "帖子详情+评论列表")
    public Result<PostDetailVO> detail(@PathVariable Integer id) {
        return Result.success(postService.detail(id));
    }

    @PostMapping("/comment")
    @Operation(summary = "发布评论")
    public Result<PostComment> addComment(@RequestBody @Validated PostCommentAddDTO dto) {
        return Result.success(postService.addComment(dto));
    }

    @PostMapping("/like/{id}")
    @Operation(summary = "点赞帖子")
    public Result<Void> like(@PathVariable Integer id) {
        postService.like(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除本人帖子")
    public Result<Void> deleteMine(@PathVariable Integer id) {
        postService.deleteMine(id);
        return Result.success();
    }
}

