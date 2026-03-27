package com.example.chatapp.repository.projection;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public String getModelCode() {
        return modelCode;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getMaxContextTokens() {
        return maxContextTokens;
    }

    public void setMaxContextTokens(Integer maxContextTokens) {
        this.maxContextTokens = maxContextTokens;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getProviderBaseUrl() {
        return providerBaseUrl;
    }

    public void setProviderBaseUrl(String providerBaseUrl) {
        this.providerBaseUrl = providerBaseUrl;
    }

    public String getProviderApiKeyCipher() {
        return providerApiKeyCipher;
    }

    public void setProviderApiKeyCipher(String providerApiKeyCipher) {
        this.providerApiKeyCipher = providerApiKeyCipher;
    }
}
