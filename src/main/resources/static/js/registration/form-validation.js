// 폼 유효성 검사 JavaScript 코드
console.log('[폼 유효성 검사 초기화]');

// 각 입력 필드를 DOM에서 참조
const userIdInput = document.getElementById('userId');
const userNicknameInput = document.getElementById('userNickname');
const passwordInput = document.getElementById('password');
const passwordConfirmInput = document.getElementById('passwordConfirm');
const emailInput = document.getElementById('email');
const verificationCodeInput = document.getElementById('verificationCode');

console.log('[DOM 요소 참조 완료]', {
    userIdInput: !!userIdInput,
    userNicknameInput: !!userNicknameInput,
    passwordInput: !!passwordInput,
    passwordConfirmInput: !!passwordConfirmInput,
    emailInput: !!emailInput,
    verificationCodeInput: !!verificationCodeInput
});

// 서버에 이메일 인증 코드를 확인하는 함수
async function verifyEmailCode(code) {
    console.log('\n[인증 코드 검증 함수 시작]');
    console.log('- 입력된 코드:', code);
    
    try {
        console.log('[서버 요청 준비] POST /verify-email-code');
        const response = await fetch('/verify-email-code', {
            method: 'POST',
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams({
                code: code
            })
        });
        console.log('[서버 응답 수신] 상태:', response.status);

        if (!response.ok) {
            console.error('[HTTP 오류]', response.status, response.statusText);
            throw new Error('서버 응답이 실패했습니다.');
        }

        console.log('[응답 데이터 읽기 시작]');
        const text = await response.text();
        console.log('[원본 응답]:', text);
        console.log('[응답 길이]:', text.length);
        console.log('[응답 문자코드]:', Array.from(text).map(c => c.charCodeAt(0)));
        
        // success라는 문자열이 포함되어 있으면 성공으로 처리
        return text.includes('success') ? 'success' : 'invalid';
    } catch (error) {
        console.error('[치명적 오류 발생]', error);
        throw error;
    }
}

// 서버 오류 표시 함수
function displayServerErrors() {
    console.log('\n[서버 오류 표시 함수 시작]');
    const serverErrors = document.querySelectorAll('.register-error');
    console.log('- 발견된 오류 요소 수:', serverErrors.length);

    let hasErrorsInCurrentStep = false;

    serverErrors.forEach((errorSpan, index) => {
        console.log(`[오류 요소 ${index + 1} 처리]`);
        errorSpan.style.display = 'none';

        const parentDiv = errorSpan.closest('.form-step');
        if (parentDiv) {
            console.log('- 단계:', parentDiv.id);
            if (parentDiv.id === `step-${currentStep}`) {
                const errorText = errorSpan.textContent.trim();
                if (errorText !== '') {
                    console.log('- 오류 메시지 표시:', errorText);
                    errorSpan.style.display = 'block';
                    hasErrorsInCurrentStep = true;
                }
            }
        }
    });

    console.log('[오류 표시 완료] 현재 단계 오류 여부:', hasErrorsInCurrentStep);
}

// 유효성 검사 규칙 로드
async function loadValidationRules() {
    try {
        const response = await fetch('/js/validation-rules.json');
        if (!response.ok) {
            throw new Error('유효성 검사 규칙을 로드할 수 없습니다.');
        }
        return await response.json();
    } catch (error) {
        console.error('[유효성 검사 규칙 로드 오류]', error);
        return null;
    }
}

// 유효성 검사 함수
async function validateField(fieldName, value) {
    const rules = await loadValidationRules();
    if (!rules || !rules[fieldName]) {
        console.warn(`[유효성 검사 규칙 없음] 필드: ${fieldName}`);
        return true;
    }

    const rule = rules[fieldName];
    if (rule.required && !value) {
        return rule.message;
    }
    if (rule.minLength && value.length < rule.minLength) {
        return rule.message;
    }
    if (rule.maxLength && value.length > rule.maxLength) {
        return rule.message;
    }
    if (rule.pattern && !new RegExp(rule.pattern).test(value)) {
        return rule.message;
    }

    return null;
}

// 예제: 필드 유효성 검사 호출
async function validateUserId() {
    const value = userIdInput.value;
    const errorMessage = await validateField('userId', value);
    if (errorMessage) {
        userIdInput.setCustomValidity(errorMessage);
    } else {
        userIdInput.setCustomValidity('');
    }
    userIdInput.reportValidity();
}

// 문서가 로드된 후 실행
document.addEventListener('DOMContentLoaded', function() {
    console.log('\n[문서 로드 완료] 이벤트 리스너 설정 시작');

    function validateUserId() {
        console.log('\n[사용자 ID 검증 시작]');
        const value = userIdInput.value;
        console.log('- 입력값:', value);
        
        if (!value) {
            console.log('- 결과: 빈 값');
            userIdInput.setCustomValidity('사용자 ID를 입력해주세요.');
        } else if (/[^a-z0-9]/.test(value)) {
            console.log('- 결과: 특수문자 포함');
            userIdInput.setCustomValidity('사용자 ID는 영소문자와 숫자만 사용할 수 있습니다.');
        } else {
            console.log('- 결과: 유효함');
            userIdInput.setCustomValidity('');
        }
        userIdInput.reportValidity();
    }

    function validateUserNickname() {
        console.log('\n[닉네임 검증 시작]');
        const value = userNicknameInput.value;
        console.log('- 입력값:', value);
        
        if (!value) {
            console.log('- 결과: 빈 값');
            userNicknameInput.setCustomValidity('사용자 닉네임을 입력해주세요.');
        } else if (/[^a-z0-9]/.test(value)) {
            console.log('- 결과: 특수문자 포함');
            userNicknameInput.setCustomValidity('사용자 닉네임은 영소문자와 숫자만 사용할 수 있습니다.');
        } else {
            console.log('- 결과: 유효함');
            userNicknameInput.setCustomValidity('');
        }
        userNicknameInput.reportValidity();
    }

    function validatePasswords() {
        console.log('\n[비밀번호 검증 시작]');
        const password = passwordInput.value;
        console.log('- 비밀번호 길이:', password.length);
        
        if (!password) {
            console.log('- 결과: 빈 값');
            passwordInput.setCustomValidity('비밀번호를 입력해주세요.');
        } else if (password.length < 4) {
            console.log('- 결과: 길이 부족');
            passwordInput.setCustomValidity('비밀번호는 4자 이상이어야 합니다.');
        } else {
            console.log('- 결과: 유효함');
            passwordInput.setCustomValidity('');
        }
        passwordInput.reportValidity();
    }

    function validateEmail() {
        console.log('\n[이메일 검증 시작]');
        const value = emailInput.value;
        console.log('- 입력값:', value);
        
        if (!value) {
            console.log('- 결과: 빈 값');
            emailInput.setCustomValidity('이메일을 입력해주세요.');
        } else if (!/^([a-z0-9]+)@([a-z0-9]+(\.[a-z]+)+)$/.test(value)) {
            console.log('- 결과: 형식 불일치');
            emailInput.setCustomValidity('이메일은 영소문자+숫자@영소문자 형식이어야 합니다.');
        } else {
            console.log('- 결과: 유효함');
            emailInput.setCustomValidity('');
        }
        emailInput.reportValidity();
    }

    // 이벤트 리스너 설정
    console.log('[이벤트 리스너 등록 시작]');
    
    userIdInput.addEventListener('keyup', validateUserId);
    userNicknameInput.addEventListener('keyup', validateUserNickname);
    passwordInput.addEventListener('keyup', validatePasswords);
    passwordConfirmInput.addEventListener('keyup', validatePasswords);
    emailInput.addEventListener('keyup', validateEmail);
    
    console.log('[이벤트 리스너 등록 완료]');

    // 폼 제출 이벤트 처리
    document.getElementById('registrationForm').addEventListener('submit', function(event) {
        console.log('\n[폼 제출 시작]');
        const allInputs = this.querySelectorAll('input[required]');
        console.log('- 필수 입력 필드 수:', allInputs.length);
        
        let formValid = true;
        let hasEmptyFields = false;

        allInputs.forEach((input, index) => {
            console.log(`\n[필드 ${index + 1} 검증]`);
            console.log('- 필드명:', input.id);
            console.log('- 값 존재:', !!input.value);
            console.log('- 유효성:', input.checkValidity());
            
            if (!input.value) {
                hasEmptyFields = true;
                console.log('- 결과: 빈 필드');
            }
            if (!input.checkValidity()) {
                formValid = false;
                console.log('- 결과: 유효성 검사 실패');
                
                const parentStepDiv = input.closest('.form-step');
                if (parentStepDiv) {
                    const stepNum = parseInt(parentStepDiv.id.replace('step-', ''));
                    console.log('- 해당 단계:', stepNum);
                    if (currentStep !== stepNum) {
                        console.log('- 단계 이동');
                        showStep(stepNum);
                    }
                }
                input.reportValidity();
            }
        });

        console.log('\n[폼 검증 결과]');
        console.log('- 빈 필드 존재:', hasEmptyFields);
        console.log('- 전체 유효성:', formValid);

        if (hasEmptyFields) {
            console.log('[제출 취소] 빈 필드');
            event.preventDefault();
            return;
        }

        console.log('- preventDefault 호출 여부:', formValid ? '아니오' : '예');
        if (!formValid) {
            event.preventDefault();
            console.log('- 폼 제출 중단');
        } else {
            console.log('- 폼 제출 진행');
        }

        console.log('[서버 오류 확인]');
        displayServerErrors();
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
