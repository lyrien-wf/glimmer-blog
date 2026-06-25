package com.blog.service;

import com.blog.dto.TagDTO;
import com.blog.model.Tag;
import com.blog.repository.ArticleTagRepository;
import com.blog.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final ArticleTagRepository articleTagRepository;

    @Autowired
    public TagService(TagRepository tagRepository, ArticleTagRepository articleTagRepository) {
        this.tagRepository = tagRepository;
        this.articleTagRepository = articleTagRepository;
    }

    public List<TagDTO> getAllTags() {
        return tagRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public TagDTO createTag(String name) {
        Tag tag = tagRepository.findByName(name).orElseGet(() -> {
            Tag newTag = new Tag();
            newTag.setName(name);
            return tagRepository.save(newTag);
        });
        return toDTO(tag);
    }

    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        articleTagRepository.deleteByTagId(id);
        tagRepository.delete(tag);
    }

    private TagDTO toDTO(Tag tag) {
        TagDTO dto = new TagDTO();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        return dto;
    }
}
