package br.edu.utfpr.api_usuario.services;

import br.edu.utfpr.api_usuario.models.UserIdSequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Service
public class UserIdSequenceService {

    @Autowired
    private MongoOperations mongoOperations;

    public long generateSequence(String seqName) {
        UserIdSequence counter = mongoOperations.findAndModify(
                Query.query(Criteria.where("_id").is(seqName)),
                new Update().inc("seq", 1),
                FindAndModifyOptions.options().returnNew(true).upsert(true),
                UserIdSequence.class
        );
        return !Objects.isNull(counter) ? counter.getSeq() : 1;
    }


}
