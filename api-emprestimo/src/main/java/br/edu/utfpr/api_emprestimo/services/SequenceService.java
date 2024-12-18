package br.edu.utfpr.api_emprestimo.services;

import br.edu.utfpr.api_emprestimo.models.Sequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SequenceService {

    @Autowired
    private MongoOperations mongoOperations;

    public long generateSequence(String seqName) {
        Sequence counter = mongoOperations.findAndModify(
                Query.query(Criteria.where("_id").is(seqName)),
                new Update().inc("seq", 1),
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                Sequence.class
        );
        return !Objects.isNull(counter) ? counter.getSeq() : 1;
    }


}
