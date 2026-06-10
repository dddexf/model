package com.example.chatapp.repository.projection;

import lombok.Data;

@Data
public class AiModelProviderView {

    private Long id;
    private Long providerId;
    private String modelCode;
    private String displayName;
    private Integer maxContextTokens;
    private Integer sortNo;
    private String providerName;
    private String providerBaseUrl;
    private String providerApiKeyCipher;
}
