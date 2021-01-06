package com.arqisoft.chemicalsearch.api;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

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
        private DateFormat dateFormat;

        public StructureController() {
                super();
                indigo = new Indigo();
                indigoRenderer = new IndigoRenderer(indigo);

                RestClientBuilder builder = RestClient.builder(new HttpHost(System.getenv("CS_ELASTICSEARCH_HOST"),
                                Integer.parseInt(System.getenv("CS_ELASTICSEARCH_PORT")),
                                System.getenv("CS_ELASTICSEARCH_SCHEME")));

                String user = System.getenv("CS_ELASTICSEARCH_USER");
                String password = System.getenv("CS_ELASTICSEARCH_PASSWORD");
                if (user != null && user.length() > 0 && password != null && password.length() > 0) {
                        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                        credentialsProvider.setCredentials(AuthScope.ANY,
                                        new UsernamePasswordCredentials(user, password));
                        builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.disableAuthCaching()
                                        .setSSLHostnameVerifier((s, sslSession) -> false)
                                        .setDefaultCredentialsProvider(credentialsProvider));
                }

                elasticClient = new RestHighLevelClient(builder);
                dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        }

        @GetMapping("/api/structures/{id}/mol")
        public ResponseEntity<String> getMolById(@PathVariable("id") String id) throws IOException {

                org.elasticsearch.action.search.SearchRequest searchRequest = new org.elasticsearch.action.search.SearchRequest(
                                System.getenv("CS_ELASTICSEARCH_INDEX"));
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.query(QueryBuilders.termQuery("_id", id));
                searchRequest.source(searchSourceBuilder);

                long time = System.nanoTime();

                SearchResponse searchResponse = elasticClient.search(searchRequest, RequestOptions.DEFAULT);
                SearchHit[] hits = searchResponse.getHits().getHits();

                time = System.nanoTime() - time;

                hits = searchResponse.getHits().getHits();
                if (hits == null || hits.length == 0) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                }
                IndigoRecord resultRecord = Helpers.fromElastic(hits[0].getId(), hits[0].getSourceAsMap(),
                                hits[0].getScore());

                IndigoObject tmpIndigoObject = resultRecord.getIndigoObject(indigo);

                System.out.println(dateFormat.format(Calendar.getInstance().getTime()) + " >> Get molecule info by id "
                                + id + " " + TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS) + " ms");

                return ResponseEntity.ok(tmpIndigoObject.molfile());
        }

        @GetMapping(path = "/api/structures/{id}/image", produces = MediaType.IMAGE_PNG_VALUE)
        public ResponseEntity<byte[]> renderMolById(@PathVariable("id") String id, @RequestParam("width") int w,
                        @RequestParam("height") int h) throws IOException {

                org.elasticsearch.action.search.SearchRequest searchRequest = new org.elasticsearch.action.search.SearchRequest(
                                System.getenv("CS_ELASTICSEARCH_INDEX"));
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                searchSourceBuilder.query(QueryBuilders.termQuery("_id", id));
                searchRequest.source(searchSourceBuilder);

                long time = System.nanoTime();

                SearchResponse searchResponse = elasticClient.search(searchRequest, RequestOptions.DEFAULT);
                SearchHit[] hits = searchResponse.getHits().getHits();

                hits = searchResponse.getHits().getHits();
                if (hits == null || hits.length == 0) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                }
                IndigoRecord resultRecord = Helpers.fromElastic(hits[0].getId(), hits[0].getSourceAsMap(),
                                hits[0].getScore());

                long searchTime = System.nanoTime() - time;

                time = System.nanoTime();

                indigo.setOption("ignore-stereochemistry-errors", true);
                indigo.setOption("ignore-noncritical-query-features", true);

                IndigoObject tmpIndigoObject = resultRecord.getIndigoObject(indigo);

                indigo.setOption("render-stereo-style", "ext");
                indigo.setOption("render-margins", 5, 5);
                indigo.setOption("render-coloring", true);
                indigo.setOption("render-relative-thickness", "1.5");
                indigo.setOption("render-image-size", w, h);
                indigo.setOption("render-output-format", "png");

                byte[] image = indigoRenderer.renderToBuffer(tmpIndigoObject);

                time = System.nanoTime() - time;

                System.out.println(dateFormat.format(Calendar.getInstance().getTime()) + " >> Render image by id "
                                + id + " Search time: " + TimeUnit.MILLISECONDS.convert(searchTime, TimeUnit.NANOSECONDS) + " ms; Render time: "+ TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS) + " ms");

                return new ResponseEntity<>(image, HttpStatus.OK);
        }
}
