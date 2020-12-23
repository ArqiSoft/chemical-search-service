package com.arqisoft.chemicalsearch.api;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigo.elastic.ElasticRepository;
import com.epam.indigo.model.FromIndigoObject;
import com.epam.indigo.model.IndigoRecord;
import com.epam.indigo.predicate.ExactMatch;
import com.epam.indigo.predicate.SimilarityMatch;
import com.epam.indigo.predicate.SubstructureMatch;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {
    protected static ElasticRepository<IndigoRecord> repository;

    public SearchController() {
        super();

    }

    public static void setElasticRepository() {
        ElasticRepository.ElasticRepositoryBuilder<IndigoRecord> builder = new ElasticRepository.ElasticRepositoryBuilder<>();
        repository = builder
                .withHostName(System.getenv("CS_ELASTICSEARCH_HOST"))
                .withPort(Integer.parseInt(System.getenv("CS_ELASTICSEARCH_PORT")))
                .withScheme(System.getenv("CS_ELASTICSEARCH_SCHEME"))
                .withIndexName(System.getenv("CS_ELASTICSEARCH_INDEX"))
                .withUserName(System.getenv("CS_ELASTICSEARCH_USER"))
                .withPassword(System.getenv("CS_ELASTICSEARCH_PASSWORD")).build();
    }

    @PostMapping("/search")
    public List<String> search(@RequestBody() final SearchRequest request) {
        if (repository == null) {
            setElasticRepository();
        }

        List<String> molecules = new ArrayList<>();
        Indigo indigo = new Indigo();
        IndigoObject indigoObject = indigo.loadMolecule(request.SmileFilter);

        IndigoRecord target = FromIndigoObject.build(indigoObject);

        List<IndigoRecord> resultRecords = new ArrayList<>();

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

        for (IndigoRecord indigoRecord : resultRecords) {
            IndigoObject tmpIndigoObject = indigoRecord.getIndigoObject(indigo);
            String mol = tmpIndigoObject.molfile();
            molecules.add(mol);
        }

        return molecules;
    }
}
