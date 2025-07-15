console.log('[폼 유효성 검사 초기화]');

// 각 입력 필드를 DOM에서 참조
const userIdInput = document.getElementById('userId');
const userNicknameInput = document.getElementById('userNickname');
const passwordInput = document.getElementById('password');
const passwordConfirmInput = document.getElementById('passwordConfirm');
const emailInput = document.getElementById('email');
const verificationCodeInput = document.getElementById('verificationCode');

let validationRules = null; // validation-rules.json 캐싱

//==============================================

/**
 * 유효성 검사 규칙 로드 (최초 1회만 fetch)
 */
async function loadValidationRules() {
    if (validationRules) return validationRules;
    try {
        const response = await fetch('/js/validation-rules.json');
        if (!response.ok) throw new Error('유효성 검사 규칙을 로드할 수 없습니다.');
        validationRules = await response.json();
        return validationRules;
    } catch (error) {
        console.error('[유효성 검사 규칙 로드 오류]', error);
        return null;
    }
}

/**
 * 단일 필드 유효성 검사 (json 기반)
 */
function validateFieldByRule(fieldName, value) {
    if (!validationRules || !validationRules[fieldName]) return null;
    const rule = validationRules[fieldName];
    if (rule.required && (!value || value.trim() === '')) {
        return rule.message;
    }
    if (rule.minLength && value.length < rule.minLength) {
        return rule.message;
    }
    if (rule.maxLength && value.length > rule.maxLength) {
        return rule.message;
    }
    if (rule.pattern && !(new RegExp(rule.pattern).test(value))) {
        return rule.message;
    }
    return null;
}

//==============================================

/**
 * 실시간 유효성 검사 이벤트 핸들러 (공통)
 */
function makeLiveValidator(inputElem, fieldName) {
    inputElem.addEventListener('keyup', async function() {
        if (!validationRules) await loadValidationRules();
        const errorMsg = validateFieldByRule(fieldName, inputElem.value);
        inputElem.setCustomValidity(errorMsg || '');
        inputElem.reportValidity();
    });
}

//==============================================

/**
 * 서버에 이메일 인증 코드를 확인하는 함수
 */
async function verifyEmailCode(code) {
    console.log('\n[인증 코드 검증 함수 시작]');
    console.log('- 입력된 코드:', code);
    
    try {
        console.log('[서버 요청 준비] POST /user/verify-email-code');
        /**
         * fetch API를 사용하여 서버에 이메일 인증 코드를 전송
         * await: 비동기 요청의 결과를 기다림
         * fetch(): Promise 기반의 비동기 요청 수행
         */
        const response = await fetch('/user/verify-email-code', {
            method: 'POST',
            // credentials: 'same-origin': 동일 출처의 쿠키만 포함
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            /**
             * HTTP 요청의 본문에 해당
             * URLSearchParams: URL 인코딩된 쿼리 문자열을 생성하는 JavaScript 객체
             * code: 서버에서 request.getParameter("code")로 이 값을 읽을 수 있음
             * 
             * // 서버에서 받음
             * @PostMapping("/verify-email-code")
             * public ResponseEntity<?> verifyEmailCode(@RequestParam String code) {
             */
            body: new URLSearchParams({
                code: code
            })
        });
        console.log('[서버 응답 수신] 상태:', response.status);

        // 응답이 실패할 경우
        if (!response.ok) {
            console.error('[HTTP 오류]', response.status, response.statusText);
            throw new Error('서버 응답이 실패했습니다.');
        }

        console.log('[응답 데이터 읽기 시작]');
        const text = await response.text();
        console.log('[원본 응답]:', text);
        console.log('[응답 길이]:', text.length);
        console.log('[응답 문자코드]:', Array.from(text).map(c => c.charCodeAt(0)));
        
        // 응답 데이터에 success라는 문자열이 포함되어 있으면 성공으로 처리
        return text.includes('success') ? 'success' : 'invalid';
    } catch (error) {
        console.error('[치명적 오류 발생]', error);
        throw error;
    }
}

//==============================================

/**
 * 서버 오류 표시 함수
 */
function displayServerErrors() {
    console.log('\n[서버 오류 표시 함수 시작]');
    const serverErrors = document.querySelectorAll('.register-error');
    console.log('- 발견된 오류 요소 수:', serverErrors.length);

    // 현재 단계에 오류가 있는지 확인
    let hasErrorsInCurrentStep = false;

    // 모든 오류 요소를 순회
    serverErrors.forEach((errorSpan, index) => {
        console.log(`[오류 요소 ${index + 1} 처리]`);
        // 이전 단계의 모든 에러 메시지들을 숨기고 현재 단계의 에러만 다시 표시하기 위해 none으로 설정
        errorSpan.style.display = 'none';

        // .form-step 클래스를 가진 가장 가까운 부모 요소를 찾음
        const parentDiv = errorSpan.closest('.form-step');
        // 부모 요소가 존재하면
        if (parentDiv) {
            console.log('- 단계:', parentDiv.id);
            // 부모 요소가 현재 단계와 일치하면
            if (parentDiv.id === `step-${currentStep}`) {
                const errorText = errorSpan.textContent.trim();
                // 오류 메시지가 비어있지 않으면
                if (errorText !== '') {
                    console.log('- 오류 메시지 표시:', errorText);
                    // block으로 설정하여 표시
                    errorSpan.style.display = 'block';
                    // 현재 단계에 오류가 있음을 표시
                    hasErrorsInCurrentStep = true;
                }
            }
        }
    });

    console.log('[오류 표시 완료] 현재 단계 오류 여부:', hasErrorsInCurrentStep);
}

//==============================================

/**
 * 문서가 로드된 후 실행
 */
document.addEventListener('DOMContentLoaded', async function() {
    // validation-rules.json 미리 로드
    await loadValidationRules();

    // 각 입력란에 실시간 유효성 검사 연결
    makeLiveValidator(userIdInput, 'userId');
    makeLiveValidator(userNicknameInput, 'userNickname');
    makeLiveValidator(passwordInput, 'password');
    makeLiveValidator(emailInput, 'email');
    makeLiveValidator(document.getElementById('name'), 'name');

    // 비밀번호 확인(일치) 검사는 별도 처리
    passwordConfirmInput.addEventListener('keyup', function() {
        if (passwordInput.value !== passwordConfirmInput.value) {
            passwordConfirmInput.setCustomValidity('비밀번호가 일치하지 않습니다.');
        } else {
            passwordConfirmInput.setCustomValidity('');
        }
        passwordConfirmInput.reportValidity();
    });

    // 폼 제출 시 모든 필드 최종 검사
    document.getElementById('registrationForm').addEventListener('submit', async function(event) {
        await loadValidationRules();
        let valid = true;
        // 모든 필드 검사
        [
            {elem: userIdInput, name: 'userId'},
            {elem: userNicknameInput, name: 'userNickname'},
            {elem: passwordInput, name: 'password'},
            {elem: emailInput, name: 'email'},
            {elem: document.getElementById('name'), name: 'name'}
        ].forEach(({elem, name}) => {
            const errorMsg = validateFieldByRule(name, elem.value);
            elem.setCustomValidity(errorMsg || '');
            if (errorMsg) valid = false;
        });
        // 비밀번호 확인
        if (passwordInput.value !== passwordConfirmInput.value) {
            passwordConfirmInput.setCustomValidity('비밀번호가 일치하지 않습니다.');
            valid = false;
        } else {
            passwordConfirmInput.setCustomValidity('');
        }
        // 인증코드(서버와 통신하는 부분은 기존대로 유지)
        if (!verificationCodeInput.value) {
            verificationCodeInput.setCustomValidity('인증 코드를 입력해주세요.');
            valid = false;
        } else {
            verificationCodeInput.setCustomValidity('');
        }

        if (!valid) {
            event.preventDefault();
        }
    });

    // 단계 전환 처리
    document.querySelectorAll('.next-step-btn').forEach((button, index) => {
        console.log(`[다음 단계 버튼 ${index + 1} 이벤트 리스너 등록]`);
        
        button.addEventListener('click', async (event) => {
            console.log('\n[다음 단계 버튼 클릭]');
            console.log('- 현재 단계:', currentStep);
            event.preventDefault();

            if (currentStep === 1) {
                console.log('[이메일 인증 단계 처리]');
                
                if (!verificationCodeInput.value) {
                    console.log('- 인증 코드 미입력');
                    verificationCodeInput.setCustomValidity('인증 코드를 입력해주세요.');
                    verificationCodeInput.reportValidity();
                    return;
                }

                try {
                    console.log('- 인증 코드 확인 시작');
                    const result = await verifyEmailCode(verificationCodeInput.value.trim());
                    console.log('- 서버 응답:', result);
                    console.log('- includes("success"):', result.includes('success'));
                    
                    if (result.includes('success')) {
                        console.log('- 인증 성공, 다음 단계로 이동');
                        showStep(currentStep + 1);
                    } else {
                        console.log('- 인증 실패');
                        alert('인증 코드가 일치하지 않습니다.');
                        verificationCodeInput.setCustomValidity('인증 코드가 일치하지 않습니다.');
                        verificationCodeInput.reportValidity();
                    }
                } catch (error) {
                    console.error('- 인증 확인 중 오류:', error);
                    alert('인증 코드 확인 중 오류가 발생했습니다.');
                }
                return;
            }

            console.log('[일반 단계 이동]');
            console.log('- 다음 단계:', currentStep + 1);
            showStep(currentStep + 1);
        });
    });
    
    console.log('[초기화 완료]\n');
});
