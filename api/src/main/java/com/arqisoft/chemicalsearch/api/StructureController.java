package com.arqisoft.chemicalsearch.api;

import java.io.IOException;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigo.IndigoRenderer;
import com.epam.indigo.model.Helpers;
import com.epam.indigo.model.IndigoRecord;

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
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class StructureController {
        private RestHighLevelClient elasticClient;
        private Indigo indigo;
        private IndigoRenderer indigoRenderer;

        public StructureController() {
                super();
                indigo = new Indigo();
                indigoRenderer = new IndigoRenderer(indigo);
        }

        @GetMapping("/api/structures/{id}/mol")
        public ResponseEntity<String> getMolById(@PathVariable("id") String id) throws IOException {
                RestClientBuilder builder = RestClient.builder(new HttpHost(System.getenv("CS_ELASTICSEARCH_HOST"),
                                Integer.parseInt(System.getenv("CS_ELASTICSEARCH_PORT")),
                                System.getenv("CS_ELASTICSEARCH_SCHEME")));

                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
                                System.getenv("CS_ELASTICSEARCH_USER"), System.getenv("CS_ELASTICSEARCH_PASSWORD")));
                builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.disableAuthCaching()
                                .setSSLHostnameVerifier((s, sslSession) -> false)
                                .setDefaultCredentialsProvider(credentialsProvider));
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
                IndigoRecord resultRecord = Helpers.fromElastic(hits[0].getId(), hits[0].getSourceAsMap(),
                                hits[0].getScore());

                IndigoObject tmpIndigoObject = resultRecord.getIndigoObject(indigo);

                return ResponseEntity.ok(tmpIndigoObject.molfile());
        }

        @GetMapping(path = "/api/structures/{id}/image", produces = MediaType.IMAGE_PNG_VALUE)
        public ResponseEntity<byte[]> renderMolById(@PathVariable("id") String id, @RequestParam("width") int w,
                        @RequestParam("height") int h) throws IOException {
                RestClientBuilder builder = RestClient.builder(new HttpHost(System.getenv("CS_ELASTICSEARCH_HOST"),
                                Integer.parseInt(System.getenv("CS_ELASTICSEARCH_PORT")),
                                System.getenv("CS_ELASTICSEARCH_SCHEME")));

                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
                                System.getenv("CS_ELASTICSEARCH_USER"), System.getenv("CS_ELASTICSEARCH_PASSWORD")));
                builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.disableAuthCaching()
                                .setSSLHostnameVerifier((s, sslSession) -> false)
                                .setDefaultCredentialsProvider(credentialsProvider));
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
                IndigoRecord resultRecord = Helpers.fromElastic(hits[0].getId(), hits[0].getSourceAsMap(),
                                hits[0].getScore());

                indigo.setOption("ignore-stereochemistry-errors", true);
                indigo.setOption("ignore-noncritical-query-features", true);

                IndigoObject tmpIndigoObject = resultRecord.getIndigoObject(indigo);

                indigo.setOption("render-stereo-style", "ext");
                indigo.setOption("render-margins", 5, 5);
                indigo.setOption("render-coloring", true);
                indigo.setOption("render-relative-thickness", "1.5");
                indigo.setOption("render-image-size", w, h);
                indigo.setOption("render-output-format", "png");

                return new ResponseEntity<>(indigoRenderer.renderToBuffer(tmpIndigoObject), HttpStatus.OK);
        }
}
