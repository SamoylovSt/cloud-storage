package ru.samoylov.cloud_storage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MinioResourceInfo  {
    @JsonProperty("path")
    private String path;
    @JsonProperty("name")
    private String name;
    @JsonProperty("size")
    private Long size;
    @JsonProperty("type")
    private String type;
}
