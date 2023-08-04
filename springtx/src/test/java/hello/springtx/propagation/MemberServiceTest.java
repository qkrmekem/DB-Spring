package hello.springtx.propagation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LogRepository logRepository;

    /*
     * memberService     @Transactional:off
     * memberRepository  @Transactional:on
     * logRepository     @Transactional:on
     * 리포지토리에서 트랜잭션 시작 - 정상 처리
     * */
    @Test
    void outerTxOff_success() {
        // given
        String username = "outerTxOff_success";

        // when
        memberService.joinV1(username);

        // then : 모든 데이터가 정상 저장된다.
        // junit의 Assertions
        // memberRepository.find(username)은 Optional을 반환
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /*
     * memberService     @Transactional:off
     * memberRepository  @Transactional:on
     * logRepository     @Transactional:on Exception
     * 리포지토리에서 트랜잭션 시작
     * 로그 리포지토리 예외 발생
     * */
    @Test
    void outerTxOff_fail() {
        // given
        String username = "로그예외_outerTxOff_fail";

        // when
        // Expecting code to raise a throwable. 발생
        // assertThatThrownBy는 예외 발생을 기대하는 메서드인데
        // 예외가 발생하지 않고 로직이 정상 처리될 경우 위의 에러가 발생함
        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        // then : 로그 예외 발생
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /*
     * memberService     @Transactional: on
     * memberRepository  @Transactional: off
     * logRepository     @Transactional: off
     * 싱글 트랜잭션 - 서비스에서 시작
     * */
    @Test
    void singleTx() {
        // given
        String username = "outerTxOff_success";

        // when
        memberService.joinV1(username);

        // then : 모든 데이터가 정상 저장된다.
        // junit의 Assertions
        // memberRepository.find(username)은 Optional을 반환
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /*
     * memberService     @Transactional: on
     * memberRepository  @Transactional: on
     * logRepository     @Transactional: on
     * 외부 트랜잭션 성공 - propagation.REQUIRES
     * */
    @Test
    void outerTxOn_success() {
        // given
        String username = "outerTxOn_success";

        // when
        memberService.joinV1(username);

        // then : 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /*
     * memberService     @Transactional: on
     * memberRepository  @Transactional: on
     * logRepository     @Transactional: on Exception
     * 외부 트랜잭션 시작 - 내부 트랜잭션 롤백
     * */
    @Test
    void outerTxOn_fail() {
        // given
        String username = "로그예외_outerTxOff_fail";

        // when
        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        // then : 모든 데이터 롤백
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /* ==== 중요!!! ======
     * memberService     @Transactional: on
     * memberRepository  @Transactional: on
     * logRepository     @Transactional: on Exception
     * 외부 트랜잭션 롤백 - 내부 트랜잭션 롤백
     * memberService.joinV2에서는 catch문을 이용해 예외를 잡았다.
     * 하지만 이미 logRepository에서 rollback이 발생한 시점에서
     * 트랜잭션 동기화 매니져에는 readOnly = true라는 속성이 부여된다.
     * memberService에서는 예외를 잡아서 처리하였기 때문에
     * commit이 작동할 것으로 예상했으나,
     * 실제로는 rollback이 발생하였다.
     * 따라서 시스템에서는 이 상황을 개발자에게 알리기 위해
     * UnexpectedRollbackException 예외를 던지고,
     * 이 예외는 서비스 바깥으로 던져진다.
     * */
    @Test
    void recoverException_fail() {
        // given
        String username = "로그예외_recoverException_fail";

        // when
        assertThatThrownBy(() -> memberService.joinV2(username))
                .isInstanceOf(UnexpectedRollbackException.class);

        // then : 모든 데이터 롤백
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /* ==== 중요!!! ======
     * memberService     @Transactional: on
     * memberRepository  @Transactional: on
     * logRepository     @Transactional: on Exception
     * 외부 트랜잭션 성공 - 내부 트랜잭션 롤백
     * */
    @Test
    void recoverException_success() {
        // given
        String username = "로그예외_recoverException_success";

        // when
        memberService.joinV2(username);

        // then : member 저장, log 롤백
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }

}