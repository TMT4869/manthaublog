package com.manthau.userservice.feature.profile;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(min = 1, max = 100, message = "Display name must be between 1 and 100 characters")
    private String displayName;

    @Pattern(regexp = "^[A-Za-z0-9]{6}$", message = "Name tag must be exactly 6 letters or digits")
    private String nameTag;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    @URL(message = "Avatar URL must be a valid URL")
    @Size(max = 500)
    private String avatarUrl;

    @URL(message = "Website must be a valid URL")
    @Size(max = 255)
    private String website;

    @Size(max = 100)
    private String location;
}
