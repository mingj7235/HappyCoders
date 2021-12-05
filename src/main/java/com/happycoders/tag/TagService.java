package com.happycoders.tag;

import com.happycoders.domain.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.TransactionScoped;

@RequiredArgsConstructor
@TransactionScoped
@Service
public class TagService {
    private final TagRepository tagRepository;

    public Tag findOrCreate (String tagTitle) {
        Tag tag = tagRepository.findByTitle(tagTitle);

        if (tag == null) {
            tag = tagRepository.save(Tag.builder().title(tagTitle).build());
        }
        return tag;
    }

}
