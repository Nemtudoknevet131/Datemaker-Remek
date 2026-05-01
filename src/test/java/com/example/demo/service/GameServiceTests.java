package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.dto.LoveArrowsResultRequest;
import com.example.demo.dto.LoveArrowsResultResponse;
import com.example.demo.model.GameStats;
import com.example.demo.model.User;
import com.example.demo.push.FireBaseManager;
import com.example.demo.repository.GameStatsRepository;
import com.example.demo.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class GameServiceTests {

    @Mock
    private GameStatsRepository gameStatsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FireBaseManager fireBaseManager;

    @InjectMocks
    private GameService gameService;

    @Test
    void submitLoveArrowsResult_updatesCoinsAndReturnsCoupleCoinVisibility() {
        User partner = new User();
        partner.setId(2L);

        User me = new User();
        me.setId(1L);
        me.setPartner(partner);

        GameStats myStats = new GameStats();
        myStats.setUser(me);
        myStats.setGameName("LOVE_ARROWS");
        myStats.setBestScore(7);
        myStats.setTotalCoins(10);

        GameStats partnerStats = new GameStats();
        partnerStats.setUser(partner);
        partnerStats.setGameName("LOVE_ARROWS");
        partnerStats.setBestScore(9);
        partnerStats.setTotalCoins(20);

        when(gameStatsRepository.findByUserAndGameName(me, "LOVE_ARROWS")).thenReturn(Optional.of(myStats));
        when(gameStatsRepository.findByUserAndGameName(partner, "LOVE_ARROWS")).thenReturn(Optional.of(partnerStats));
        when(gameStatsRepository.save(myStats)).thenReturn(myStats);

        LoveArrowsResultResponse response = gameService.submitLoveArrowsResult(
                me,
                new LoveArrowsResultRequest(12, 3, false));

        assertThat(myStats.getTotalCoins()).isEqualTo(13);
        assertThat(response.myCoins()).isEqualTo(13);
        assertThat(response.partnerCoins()).isEqualTo(20);
        assertThat(response.coupleCoins()).isEqualTo(33);

        verify(gameStatsRepository).save(myStats);
        verify(fireBaseManager, never()).sendMessage(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void getLoveArrowsSummary_returnsCoupleCoinsWhenBothPartnersHaveStats() {
        User partner = new User();
        partner.setId(2L);

        User me = new User();
        me.setId(1L);
        me.setPartner(partner);

        GameStats myStats = new GameStats();
        myStats.setBestScore(15);
        myStats.setTotalCoins(40);

        GameStats partnerStats = new GameStats();
        partnerStats.setBestScore(11);
        partnerStats.setTotalCoins(30);

        when(gameStatsRepository.findByUserAndGameName(me, "LOVE_ARROWS")).thenReturn(Optional.of(myStats));
        when(gameStatsRepository.findByUserAndGameName(partner, "LOVE_ARROWS")).thenReturn(Optional.of(partnerStats));

        LoveArrowsResultResponse response = gameService.getLoveArrowsSummary(me);

        assertThat(response.myHighScore()).isEqualTo(15);
        assertThat(response.partnerHighScore()).isEqualTo(11);
        assertThat(response.myCoins()).isEqualTo(40);
        assertThat(response.partnerCoins()).isEqualTo(30);
        assertThat(response.coupleCoins()).isEqualTo(70);
    }
}
