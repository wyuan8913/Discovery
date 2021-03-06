package com.nepxion.discovery.console.desktop.controller;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nepxion.discovery.console.desktop.context.PropertiesContext;
import com.nepxion.discovery.console.desktop.entity.InstanceEntity;
import com.nepxion.discovery.console.desktop.entity.ResultEntity;
import com.nepxion.discovery.console.desktop.entity.RouterEntity;
import com.nepxion.discovery.console.desktop.serializer.JacksonSerializer;

public class ServiceController {
    public static RestTemplate restTemplate;

    static {
        restTemplate = new RestTemplate();
    }

    public static Map<String, List<InstanceEntity>> getInstanceMap() {
        String url = getUrl() + "/console/instance-map";

        String json = restTemplate.getForEntity(url, String.class).getBody();

        return convert(json, new TypeReference<Map<String, List<InstanceEntity>>>() {
        });
    }

    public static List<String> getVersions(InstanceEntity instance) {
        String url = "http://" + instance.getHost() + ":" + instance.getPort() + "/version/view";

        String json = restTemplate.getForEntity(url, String.class).getBody();

        return convert(json, new TypeReference<List<String>>() {
        });
    }

    public static List<String> getRules(InstanceEntity instance) {
        String url = "http://" + instance.getHost() + ":" + instance.getPort() + "/config/view";

        String json = restTemplate.getForEntity(url, String.class).getBody();

        return convert(json, new TypeReference<List<String>>() {
        });
    }

    public static RouterEntity routes(InstanceEntity instance, String routeServiceIds) {
        String url = "http://" + instance.getHost() + ":" + instance.getPort() + "/router/routes";

        String json = restTemplate.postForEntity(url, routeServiceIds, String.class).getBody();

        return convert(json, new TypeReference<RouterEntity>() {
        });
    }

    public static List<ResultEntity> configUpdate(String serviceId, String config) {
        String url = getUrl() + "/console/config/update-sync/" + serviceId;

        // 解决中文乱码
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity<String> entity = new HttpEntity<String>(config, headers);

        String json = restTemplate.postForEntity(url, entity, String.class).getBody();

        return convert(json, new TypeReference<List<ResultEntity>>() {
        });
    }

    public static List<ResultEntity> configClear(String serviceId) {
        String url = getUrl() + "/console/config/clear/" + serviceId;

        String json = restTemplate.postForEntity(url, null, String.class).getBody();

        return convert(json, new TypeReference<List<ResultEntity>>() {
        });
    }

    private static String getUrl() {
        String url = PropertiesContext.getProperties().getString("url");
        if (!url.endsWith("/")) {
            url += "/";
        }

        return url;
    }

    private static <T> T convert(String json, TypeReference<T> typeReference) {
        try {
            return JacksonSerializer.fromJson(json, typeReference);
        } catch (Exception e) {
            throw new IllegalArgumentException(json);
        }
    }
}