// src/main/java/com/hospital/ouc/registrationsystem/web/dto/PatientDTO.java
package com.hospital.ouc.registrationsystem.web.dto;

import com.hospital.ouc.registrationsystem.domain.enums.Gender;
import com.hospital.ouc.registrationsystem.domain.validation.AddGroup;
import com.hospital.ouc.registrationsystem.domain.validation.UpdateGroup;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PatientDTO {
    private Long id; // 患者档案ID（后端生成，前端无需传）
    private Long userId; // 关联用户ID（后端生成，前端无需传）

    @NotBlank(message = "用户名不能为空", groups = AddGroup.class)
    private String username; // 登录用户名

    // 核心修正：正则转义符+完整身份证校验（支持18位纯数字/最后一位X/x）
    @NotBlank(message = "身份证号不能为空", groups = AddGroup.class)
    @Pattern(
            regexp = "^[1-9]\\d{16}(\\d|X|x)$", // 正确转义：\\d 代表数字
            message = "身份证号格式错误，请输入18位合法身份证号（最后一位可填X/x）",
            groups = {AddGroup.class, UpdateGroup.class}
    )
    private String idCard;

    @NotBlank(message = "姓名不能为空", groups = AddGroup.class)
    private String name;

    @NotBlank(message = "手机号不能为空", groups = AddGroup.class)
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误", groups = {AddGroup.class, UpdateGroup.class})
    private String phoneNumber;

    @Min(value = 0, message = "年龄不能小于0", groups = {AddGroup.class, UpdateGroup.class})
    @Max(value = 150, message = "年龄不能大于150", groups = {AddGroup.class, UpdateGroup.class})
    private Integer age;

    @NotNull(message = "性别不能为空", groups = AddGroup.class)
    private Gender gender; // 仅支持 male/female

    private Boolean isActive; // 后端控制（前端无需传）
}