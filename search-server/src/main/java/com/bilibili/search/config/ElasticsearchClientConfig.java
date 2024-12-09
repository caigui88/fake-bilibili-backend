package com.bilibili.search.config;

import com.bilibili.search.config.properties.ElasticsearchClientProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
@EnableConfigurationProperties(ElasticsearchClientProperties.class)
public class ElasticsearchClientConfig {

    private final ElasticsearchClientProperties properties;

    public ElasticsearchClientConfig(ElasticsearchClientProperties elasticsearchClientProperties) {
        this.properties = elasticsearchClientProperties;
    }

    @ConditionalOnMissingBean(RestHighLevelClient.class)
    @Bean
    public RestHighLevelClient restHighLevelClient() {
        if(properties==null){
            log.error("properties==null");
        }else {
            List<String> list = properties.getHosts();
            if(list.size()==0){
                log.error("list==0");
            }
        }

        HttpHost[] hosts = properties.getHosts().stream()
                .map(HttpHost::create).toArray(HttpHost[]::new);
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(hosts)
                        .setHttpClientConfigCallback(httpClientBuilder ->
                                httpClientBuilder.setDefaultIOReactorConfig(IOReactorConfig.custom()
                                        .setIoThreadCount(8).build())
                        )
                        .setRequestConfigCallback(requestConfigBuilder ->
                                requestConfigBuilder.setConnectTimeout(300000)
                                        .setSocketTimeout(300000)
                        )
        );

        log.info("Connecting Elasticsearch...");
        while (true) {
            try {
                if (client.ping(RequestOptions.DEFAULT)) {
                    break;
                }
            } catch (Exception e) {
                log.warn("Connecting Elasticsearch Failed, Retry in 10 seconds...");
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e1) {
                    log.error("", e1);
                }
            }
        }
        log.info("Elasticsearch Connected!");
        return client;
    }
}
