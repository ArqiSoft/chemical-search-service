package com.arqisoft.chemicalsearch.api;

import java.io.IOException;
import java.net.http.HttpHeaders;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigo.IndigoRenderer;
import com.epam.indigo.elastic.ElasticRepository;
import com.epam.indigo.model.FromIndigoObject;
import com.epam.indigo.model.Helpers;
import com.epam.indigo.model.IndigoRecord;
import com.epam.indigo.model.IndigoRecord.IndigoRecordBuilder;
import com.epam.indigo.predicate.ExactMatch;
import com.epam.indigo.predicate.SimilarityMatch;
import com.epam.indigo.predicate.SubstructureMatch;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class StructureController {
    private RestHighLevelClient elasticClient;

    @GetMapping("/api/structures/{id}/mol")
    public ResponseEntity<String> getMolById(@PathVariable("id") String id) throws IOException {
        RestClientBuilder builder = RestClient.builder(new HttpHost(System.getenv("CS_ELASTICSEARCH_HOST"),
                Integer.parseInt(System.getenv("CS_ELASTICSEARCH_PORT")), System.getenv("CS_ELASTICSEARCH_SCHEME")));

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
                System.getenv("CS_ELASTICSEARCH_USER"), System.getenv("CS_ELASTICSEARCH_PASSWORD")));
        builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.disableAuthCaching()
                .setSSLHostnameVerifier((s, sslSession) -> false).setDefaultCredentialsProvider(credentialsProvider));
        elasticClient = new RestHighLevelClient(builder);

        org.elasticsearch.action.search.SearchRequest searchRequest = new org.elasticsearch.action.search.SearchRequest(
                System.getenv("CS_ELASTICSEARCH_INDEX"));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("_id", id));
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = elasticClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] hits = searchResponse.getHits().getHits();

        hits = searchResponse.getHits().getHits();
        if (hits == null || hits.length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        IndigoRecord resultRecord = Helpers.fromElastic(hits[0].getId(), hits[0].getSourceAsMap(), hits[0].getScore());

        Indigo indigo = new Indigo();
        IndigoObject tmpIndigoObject = resultRecord.getIndigoObject(indigo);

        return ResponseEntity.ok(tmpIndigoObject.molfile());
    }

    @GetMapping("/api/structures/{id}/image")
    public ResponseEntity<byte[]> renderMolById(@PathVariable("id") String id) throws IOException {
        RestClientBuilder builder = RestClient.builder(new HttpHost(System.getenv("CS_ELASTICSEARCH_HOST"),
                Integer.parseInt(System.getenv("CS_ELASTICSEARCH_PORT")), System.getenv("CS_ELASTICSEARCH_SCHEME")));

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
                System.getenv("CS_ELASTICSEARCH_USER"), System.getenv("CS_ELASTICSEARCH_PASSWORD")));
        builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.disableAuthCaching()
                .setSSLHostnameVerifier((s, sslSession) -> false).setDefaultCredentialsProvider(credentialsProvider));
        elasticClient = new RestHighLevelClient(builder);

        org.elasticsearch.action.search.SearchRequest searchRequest = new org.elasticsearch.action.search.SearchRequest(
                System.getenv("CS_ELASTICSEARCH_INDEX"));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("_id", id));
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = elasticClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHit[] hits = searchResponse.getHits().getHits();

        hits = searchResponse.getHits().getHits();
        if (hits == null || hits.length == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        IndigoRecord resultRecord = Helpers.fromElastic(hits[0].getId(), hits[0].getSourceAsMap(), hits[0].getScore());

        Indigo indigo = new Indigo();
        IndigoRenderer indigoRenderer = new IndigoRenderer(indigo);
        IndigoObject tmpIndigoObject = resultRecord.getIndigoObject(indigo);
        indigo.setOption("render-output-format", "png");

        return new ResponseEntity<>(indigoRenderer.renderToBuffer(tmpIndigoObject), HttpStatus.OK);
    }
}
