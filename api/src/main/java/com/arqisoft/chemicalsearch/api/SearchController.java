package com.arqisoft.chemicalsearch.api;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigo.elastic.ElasticRepository;
import com.epam.indigo.model.FromIndigoObject;
import com.epam.indigo.model.IndigoRecord;
import com.epam.indigo.predicate.ExactMatch;
import com.epam.indigo.predicate.SimilarityMatch;
import com.epam.indigo.predicate.SubstructureMatch;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class SearchController {
    protected static ElasticRepository<IndigoRecord> repository;

    public SearchController() {
        super();

    }

    public static void setElasticRepository() {
        ElasticRepository.ElasticRepositoryBuilder<IndigoRecord> builder = new ElasticRepository.ElasticRepositoryBuilder<>();
        repository = builder.withHostName(System.getenv("CS_ELASTICSEARCH_HOST"))
                .withPort(Integer.parseInt(System.getenv("CS_ELASTICSEARCH_PORT")))
                .withScheme(System.getenv("CS_ELASTICSEARCH_SCHEME"))
                .withIndexName(System.getenv("CS_ELASTICSEARCH_INDEX"))
                .withUserName(System.getenv("CS_ELASTICSEARCH_USER"))
                .withPassword(System.getenv("CS_ELASTICSEARCH_PASSWORD")).build();
    }

    @PostMapping("/api/search")
    public ResponseEntity<List<ResultRecord>> search(@RequestBody() final SearchRequest request) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        List<ResultRecord> result = new ArrayList<>();

        if(repository == null){
            setElasticRepository();
        }

        if (request.Limit == null || request.Limit == 0) {
            request.Limit = 100;
        }

        if (request.Threshold == 0) {
            request.Threshold = (float) 0.9;
        }

        Indigo indigo = new Indigo();

        IndigoObject indigoObject = null;

        try {
            indigoObject = indigo.loadMolecule(request.SmileFilter);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        if (indigoObject == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        IndigoRecord target = FromIndigoObject.build(indigoObject);

        List<IndigoRecord> resultRecords = new ArrayList<>();

        long time = System.nanoTime();

        switch (request.SearchType) {
            case SIMILARITYMATCH:
                resultRecords = repository.stream().filter(new SimilarityMatch<>(target, request.Threshold))
                        .limit(request.Limit).collect(Collectors.toList());
                break;
            case EXACTMATCH:
                resultRecords = repository.stream().filter(new ExactMatch<>(target)).limit(request.Limit)
                        .collect(Collectors.toList()).stream().filter(ExactMatch.exactMatchAfterChecker(target, indigo))
                        .collect(Collectors.toList());
                break;
            case SUBSTRUCTUREMATCH:
                resultRecords = repository.stream().filter(new SubstructureMatch<>(target)).limit(request.Limit)
                        .collect(Collectors.toList()).stream()
                        .filter(SubstructureMatch.substructureMatchAfterChecker(target, indigo))
                        .collect(Collectors.toList());
                break;
            default:
                break;
        }

        time = System.nanoTime() - time;
        System.out.println(dateFormat.format(Calendar.getInstance().getTime()) + " >> Smile filter: " + request.SmileFilter + "; Search type: " + request.SearchType + "ж Search time: " + TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS) + "ms");


        for (IndigoRecord indigoRecord : resultRecords) {
            ResultRecord tmp = new ResultRecord();
            tmp.Id = indigoRecord.getInternalID();
            tmp.Score = indigoRecord.getScore();
            result.add(tmp);
        }

        return ResponseEntity.ok(result);
    }
}
