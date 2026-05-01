package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.CategoryDto;
import com.example.demo.dto.IdeaDto;
import com.example.demo.dto.ReactionRequest;
import com.example.demo.model.Category;
import com.example.demo.model.Idea;
import com.example.demo.model.IdeaReaction;
import com.example.demo.model.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.IdeaReactionRepository;
import com.example.demo.repository.IdeaRepository;
import com.example.demo.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class InspirationServiceTests {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private IdeaRepository ideaRepository;

    @Mock
    private IdeaReactionRepository ideaReactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InspirationService inspirationService;

    @Test
    void createIdea_savesIdeaWithLikeCountZero() {
        User user = new User();
        user.setId(1L);
        user.setEmail("me@example.com");

        Category category = new Category();
        category.setId(10L);
        category.setName("Romantic");

        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(user));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(ideaRepository.save(any(Idea.class))).thenAnswer(invocation -> {
            Idea idea = invocation.getArgument(0);
            idea.setId(55L);
            return idea;
        });

        IdeaDto result = inspirationService.createIdea(
                "me@example.com",
                10L,
                "Sunset picnic",
                "Bring snacks",
                "img.png");

        assertThat(result.getId()).isEqualTo(55L);
        assertThat(result.getLikeCount()).isZero();
        assertThat(result.getCurrentUserReaction()).isEqualTo("NONE");

        ArgumentCaptor<Idea> captor = ArgumentCaptor.forClass(Idea.class);
        verify(ideaRepository).save(captor.capture());
        assertThat(captor.getValue().getCreatedBy()).isEqualTo(user);
        assertThat(captor.getValue().getCategory()).isEqualTo(category);
        assertThat(captor.getValue().getLikeCount()).isZero();
    }

    @Test
    void reactToIdea_createsLikeAndUpdatesLikeCount() {
        User user = new User();
        user.setId(1L);

        Idea idea = new Idea();
        idea.setId(33L);

        ReactionRequest req = new ReactionRequest(33L, IdeaReaction.ReactionType.LIKE);

        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(user));
        when(ideaRepository.findById(33L)).thenReturn(Optional.of(idea));
        when(ideaReactionRepository.findByIdeaAndUser(idea, user)).thenReturn(Optional.empty());
        when(ideaReactionRepository.countByIdeaAndType(idea, IdeaReaction.ReactionType.LIKE)).thenReturn(1);
        when(ideaRepository.save(idea)).thenReturn(idea);

        IdeaDto result = inspirationService.reactToIdea("me@example.com", req);

        verify(ideaReactionRepository).save(any(IdeaReaction.class));
        verify(ideaRepository).save(idea);
        assertThat(idea.getLikeCount()).isEqualTo(1);
        assertThat(result.getLikeCount()).isEqualTo(1);
        assertThat(result.getCurrentUserReaction()).isEqualTo("LIKE");
    }

    @Test
    void reactToIdea_togglesOffWhenSameReactionExists() {
        User user = new User();
        user.setId(1L);

        Idea idea = new Idea();
        idea.setId(33L);

        IdeaReaction existing = new IdeaReaction();
        existing.setIdea(idea);
        existing.setUser(user);
        existing.setType(IdeaReaction.ReactionType.LIKE);

        ReactionRequest req = new ReactionRequest(33L, IdeaReaction.ReactionType.LIKE);

        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(user));
        when(ideaRepository.findById(33L)).thenReturn(Optional.of(idea));
        when(ideaReactionRepository.findByIdeaAndUser(idea, user)).thenReturn(Optional.of(existing));
        when(ideaReactionRepository.countByIdeaAndType(idea, IdeaReaction.ReactionType.LIKE)).thenReturn(0);
        when(ideaRepository.save(idea)).thenReturn(idea);

        IdeaDto result = inspirationService.reactToIdea("me@example.com", req);

        verify(ideaReactionRepository).delete(existing);
        verify(ideaReactionRepository, never()).save(existing);
        assertThat(idea.getLikeCount()).isZero();
        assertThat(result.getLikeCount()).isZero();
    }

    @Test
    void getAllCategories_mapsBackendDataToFrontendDtos() {
        User currentUser = new User();
        currentUser.setId(1L);

        Category category = new Category();
        category.setId(7L);
        category.setName("Adventure");

        Idea idea = new Idea();
        idea.setId(99L);
        idea.setTitle("Kayak date");
        idea.setDescription("River route");
        idea.setImageUrl("/img/kayak.jpg");
        idea.setLikeCount(4);

        IdeaReaction reaction = new IdeaReaction();
        reaction.setIdea(idea);
        reaction.setUser(currentUser);
        reaction.setType(IdeaReaction.ReactionType.DISLIKE);

        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(currentUser));
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(ideaRepository.findByCategoryOrderByLikeCountDesc(category)).thenReturn(List.of(idea));
        when(ideaReactionRepository.findByIdeaAndUser(idea, currentUser)).thenReturn(Optional.of(reaction));

        List<CategoryDto> categories = inspirationService.getAllCategories("me@example.com");

        assertThat(categories).hasSize(1);
        CategoryDto dto = categories.get(0);
        assertThat(dto.getName()).isEqualTo("Adventure");
        assertThat(dto.getIdeas()).hasSize(1);
        assertThat(dto.getIdeas().get(0).getCurrentUserReaction()).isEqualTo("DISLIKE");
    }
}
