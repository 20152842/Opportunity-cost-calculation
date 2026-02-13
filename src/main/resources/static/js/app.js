// 프리셋 데이터
const presets = {
    mart: {
        hourlyWage: 15000,
        options: [
            { timeMinutes: 10, directCost: 3000 },   // A: 가까운 마트 (빠르고 조금 비쌈)
            { timeMinutes: 40, directCost: 2300 },   // B: 먼 마트 (느리고 쌈)
            { timeMinutes: 25, directCost: 2800 },   // C: 중간 거리 마트
            { timeMinutes: 5, directCost: 3500 },    // D: 편의점 (매우 빠르고 비쌈)
            { timeMinutes: 60, directCost: 2000 }    // E: 대형마트 (매우 느리고 가장 쌈)
        ],
        // 하위 호환성을 위해 유지
        optionA: { timeMinutes: 10, directCost: 3000 },
        optionB: { timeMinutes: 40, directCost: 2300 }
    },
    cooking: {
        hourlyWage: 20000,
        options: [
            { timeMinutes: 60, directCost: 6000 },   // A: 직접 요리 (시간 많이, 비용 적음)
            { timeMinutes: 10, directCost: 14000 },  // B: 배달 (빠르고 비쌈)
            { timeMinutes: 30, directCost: 8000 },   // C: 간편식 (중간)
            { timeMinutes: 90, directCost: 5000 },   // D: 본격 요리 (시간 더 많이, 비용 최소)
            { timeMinutes: 5, directCost: 16000 }    // E: 고급 배달 (매우 빠르고 매우 비쌈)
        ],
        optionA: { timeMinutes: 60, directCost: 6000 },
        optionB: { timeMinutes: 10, directCost: 14000 }
    },
    delivery: {
        hourlyWage: 15000,
        options: [
            { timeMinutes: 120, directCost: 0 },     // A: 무료배송 기다리기 (느리고 무료)
            { timeMinutes: 10, directCost: 3000 },   // B: 당일배송 (빠르고 유료)
            { timeMinutes: 60, directCost: 1500 },   // C: 일반배송 (중간 속도, 중간 가격)
            { timeMinutes: 5, directCost: 5000 },    // D: 새벽배송 (매우 빠르고 비쌈)
            { timeMinutes: 180, directCost: 0 }      // E: 무료배송 느린 옵션
        ],
        optionA: { timeMinutes: 120, directCost: 0 },
        optionB: { timeMinutes: 10, directCost: 3000 }
    }
};

// DOM 요소
const hourlyWageInput = document.getElementById('hourlyWage');
const perMinuteValueSpan = document.getElementById('perMinuteValue');
const optionATimeInput = document.getElementById('optionA-time');
const optionACostInput = document.getElementById('optionA-cost');
const optionBTimeInput = document.getElementById('optionB-time');
const optionBCostInput = document.getElementById('optionB-cost');
const calculateBtn = document.getElementById('calculateBtn');
const resultSection = document.getElementById('resultSection');
const errorMessage = document.getElementById('errorMessage');

// 시급 입력 시 분당 가치 업데이트
hourlyWageInput.addEventListener('input', () => {
    const hourlyWage = parseFloat(hourlyWageInput.value);
    if (hourlyWage > 0) {
        const perMinute = hourlyWage / 60;
        perMinuteValueSpan.textContent = Math.floor(perMinute).toLocaleString();
    } else {
        perMinuteValueSpan.textContent = '-';
    }
});

// 프리셋 버튼 이벤트
document.querySelectorAll('.preset-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        const preset = presets[btn.dataset.preset];
        if (!preset) return;

        console.log('프리셋 클릭:', btn.dataset.preset, 'currentMode:', currentMode);

        // 공통: 시급 설정 + 분당 가치 업데이트
        hourlyWageInput.value = preset.hourlyWage;
        hourlyWageInput.dispatchEvent(new Event('input'));

        if (currentMode === 'multi') {
            console.log('다안 비교 모드에서 프리셋 적용 시도');
            console.log('현재 선택지 개수:', multiOptionCount);
            
            // 다안 비교 모드: 모든 선택지를 프리셋의 options 배열로 채움
            let filledCount = 0;
            for (let i = 0; i < multiOptionCount; i++) {
                const timeInput = document.querySelector(`.multi-time-input[data-index="${i}"]`);
                const costInput = document.querySelector(`.multi-cost-input[data-index="${i}"]`);
                
                if (timeInput && costInput && preset.options && preset.options[i]) {
                    timeInput.value = preset.options[i].timeMinutes;
                    costInput.value = preset.options[i].directCost;
                    filledCount++;
                    console.log(`선택지 ${String.fromCharCode(65 + i)} 채움:`, preset.options[i]);
                } else if (timeInput && costInput) {
                    console.warn(`선택지 ${String.fromCharCode(65 + i)}: 프리셋 데이터 없음, 빈 값으로 유지`);
                }
            }
            
            if (filledCount > 0) {
                console.log(`프리셋 적용 완료: ${filledCount}개 선택지 채움`);
            } else {
                console.error('입력 필드를 찾을 수 없음');
                // 에러 메시지 표시 (alert는 제거하고 하단 메시지와 스크롤만)
                errorMessage.textContent = '다안 비교 입력 필드를 찾을 수 없습니다. 다안 비교 모드로 먼저 전환해주세요.';
                errorMessage.classList.remove('hidden');
                window.scrollTo({ top: 0, behavior: 'smooth' });
            }
        } else {
            // 기본 2안 비교 모드에서는 기존 A/B 입력 필드를 채움
            optionATimeInput.value = preset.optionA.timeMinutes;
            optionACostInput.value = preset.optionA.directCost;
            optionBTimeInput.value = preset.optionB.timeMinutes;
            optionBCostInput.value = preset.optionB.directCost;
        }
    });
});

// 기본 계산(2개 선택지) 호출 함수
async function calculateTwoOptions() {
    // 입력 검증
    if (!validateInputs()) {
        return;
    }

    // 로딩 상태 표시
    setLoadingState(true);

    // 요청 데이터 구성
    const requestData = {
        hourlyWage: parseInt(hourlyWageInput.value.trim()),
        optionA: {
            timeMinutes: parseInt(optionATimeInput.value.trim()),
            directCost: parseInt(optionACostInput.value.trim())
        },
        optionB: {
            timeMinutes: parseInt(optionBTimeInput.value.trim()),
            directCost: parseInt(optionBCostInput.value.trim())
        }
    };

    try {
        // API 호출
        const response = await fetch('/api/calculate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        });

        if (!response.ok) {
            let errorMessage = '계산 중 오류가 발생했습니다.';
            try {
                const errorData = await response.json();
                errorMessage = formatValidationErrors(errorData);
            } catch (e) {
                errorMessage = `서버 오류 (${response.status}): ${response.statusText}`;
            }
            showError(errorMessage);
            return;
        }

        const result = await response.json();
        displayResult(result);
        saveToHistory('2', requestData, result);
        hideError();
    } catch (error) {
        showError('네트워크 오류가 발생했습니다: ' + error.message);
    } finally {
        setLoadingState(false);
    }
}

// 입력 검증
function validateInputs() {
    // 빈 문자열 체크 및 숫자 변환
    const hourlyWageStr = hourlyWageInput.value.trim();
    const optionATimeStr = optionATimeInput.value.trim();
    const optionACostStr = optionACostInput.value.trim();
    const optionBTimeStr = optionBTimeInput.value.trim();
    const optionBCostStr = optionBCostInput.value.trim();

    if (!hourlyWageStr || hourlyWageStr === '') {
        showError('시급을 입력해주세요.');
        hourlyWageInput.focus();
        return false;
    }

    const hourlyWage = parseFloat(hourlyWageStr);
    if (isNaN(hourlyWage) || hourlyWage < 1) {
        showError('시급은 1원 이상의 숫자여야 합니다.');
        hourlyWageInput.focus();
        return false;
    }
    
    // 상한선 체크 (1억원)
    if (hourlyWage > 100000000) {
        showError('시급은 1억원 이하로 입력해주세요.');
        hourlyWageInput.focus();
        return false;
    }

    // 큰 값 경고 (1,000,000원 이상)
    if (hourlyWage >= 1000000) {
        if (!confirm(`입력하신 시급(${hourlyWage.toLocaleString()}원)이 매우 높습니다. 계속하시겠습니까?`)) {
            return false;
        }
    }

    if (!optionATimeStr || optionATimeStr === '') {
        showError('선택지 A의 소요 시간을 입력해주세요.');
        optionATimeInput.focus();
        return false;
    }

    const optionATime = parseInt(optionATimeStr);
    if (isNaN(optionATime) || optionATime < 0) {
        showError('선택지 A의 소요 시간은 0분 이상의 숫자여야 합니다.');
        optionATimeInput.focus();
        return false;
    }

    if (!optionACostStr || optionACostStr === '') {
        showError('선택지 A의 직접 비용을 입력해주세요.');
        optionACostInput.focus();
        return false;
    }

    const optionACost = parseInt(optionACostStr);
    if (isNaN(optionACost) || optionACost < 0) {
        showError('선택지 A의 직접 비용은 0원 이상의 숫자여야 합니다.');
        optionACostInput.focus();
        return false;
    }

    if (!optionBTimeStr || optionBTimeStr === '') {
        showError('선택지 B의 소요 시간을 입력해주세요.');
        optionBTimeInput.focus();
        return false;
    }

    const optionBTime = parseInt(optionBTimeStr);
    if (isNaN(optionBTime) || optionBTime < 0) {
        showError('선택지 B의 소요 시간은 0분 이상의 숫자여야 합니다.');
        optionBTimeInput.focus();
        return false;
    }

    if (!optionBCostStr || optionBCostStr === '') {
        showError('선택지 B의 직접 비용을 입력해주세요.');
        optionBCostInput.focus();
        return false;
    }

    const optionBCost = parseInt(optionBCostStr);
    if (isNaN(optionBCost) || optionBCost < 0) {
        showError('선택지 B의 직접 비용은 0원 이상의 숫자여야 합니다.');
        optionBCostInput.focus();
        return false;
    }

    return true;
}

// 결과 표시
function displayResult(result) {
    // 추천 선택지
    const recommendationText = document.getElementById('recommendationText');
    const recommendation = result.recommendation;
    if (recommendation === '동일') {
        recommendationText.textContent = '차이 없음 (동일)';
    } else {
        recommendationText.textContent = `선택지 ${recommendation} 추천`;
    }

    // 차액
    document.getElementById('differenceText').textContent = 
        result.costDifference.toLocaleString();

    // 선택지 A 결과
    document.getElementById('resultA-direct').textContent = 
        result.optionA.directCost.toLocaleString();
    document.getElementById('resultA-time').textContent = 
        result.optionA.timeCost.toLocaleString();
    document.getElementById('resultA-total').textContent = 
        result.optionA.totalCost.toLocaleString();

    // 선택지 B 결과
    document.getElementById('resultB-direct').textContent = 
        result.optionB.directCost.toLocaleString();
    document.getElementById('resultB-time').textContent = 
        result.optionB.timeCost.toLocaleString();
    document.getElementById('resultB-total').textContent = 
        result.optionB.totalCost.toLocaleString();

    // 계산식 - 상세 버전 (각 선택지별로)
    const hourlyWage = parseInt(hourlyWageInput.value.trim());
    const perMinute = (hourlyWage / 60).toFixed(0);
    const timeA = parseInt(optionATimeInput.value.trim());
    const timeB = parseInt(optionBTimeInput.value.trim());
    
    const detailedFormula = `📊 계산 공식: 총 비용 = 직접 비용 + (시급 ÷ 60) × 소요 시간(분)
분당 가치 = ${hourlyWage.toLocaleString()}원/시간 ÷ 60 = ${perMinute}원/분

【선택지 A 상세 계산】
・직접 비용: ${result.optionA.directCost.toLocaleString()}원
・시간 비용: ${perMinute}원/분 × ${timeA}분 = ${result.optionA.timeCost.toLocaleString()}원
・총 비용: ${result.optionA.directCost.toLocaleString()}원 + ${result.optionA.timeCost.toLocaleString()}원 = ${result.optionA.totalCost.toLocaleString()}원

【선택지 B 상세 계산】
・직접 비용: ${result.optionB.directCost.toLocaleString()}원
・시간 비용: ${perMinute}원/분 × ${timeB}분 = ${result.optionB.timeCost.toLocaleString()}원
・총 비용: ${result.optionB.directCost.toLocaleString()}원 + ${result.optionB.timeCost.toLocaleString()}원 = ${result.optionB.totalCost.toLocaleString()}원

✅ 결론: ${recommendation === '동일' ? '두 선택지의 총 비용이 동일합니다' : `선택지 ${recommendation}이(가) ${result.costDifference.toLocaleString()}원 더 유리합니다`}`;

    document.getElementById('formulaText').textContent = detailedFormula;

    // 결과 섹션 표시
    resultSection.classList.remove('hidden');
    
    // 결과 섹션으로 스크롤
    resultSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

// 계산식 토글
document.getElementById('formulaToggle').addEventListener('click', () => {
    const formulaContent = document.getElementById('formulaContent');
    const formulaToggle = document.getElementById('formulaToggle');
    formulaContent.classList.toggle('hidden');
    if (formulaContent.classList.contains('hidden')) {
        formulaToggle.textContent = '📐 계산식 보기';
    } else {
        formulaToggle.textContent = '📐 계산식 숨기기';
    }
});

// Enter 키로 계산하기
document.addEventListener('keypress', (e) => {
    if (e.key === 'Enter' && !calculateBtn.disabled) {
        calculateBtn.click();
    }
});

// 에러 메시지 표시
function showError(message) {
    console.error('에러 발생:', message);
    
    // 1. 하단 에러 메시지 표시
    errorMessage.textContent = message;
    errorMessage.classList.remove('hidden');
    
    // 2. 상단으로 스크롤
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

// 에러 메시지 숨김
function hideError() {
    errorMessage.classList.add('hidden');
}

// 검증 오류 포맷팅
function formatValidationErrors(errors) {
    const messages = [];
    const fieldNames = {
        'hourlyWage': '시급',
        'optionA.timeMinutes': '선택지 A의 소요 시간',
        'optionA.directCost': '선택지 A의 직접 비용',
        'optionB.timeMinutes': '선택지 B의 소요 시간',
        'optionB.directCost': '선택지 B의 직접 비용'
    };
    
    for (const [field, message] of Object.entries(errors)) {
        const fieldName = fieldNames[field] || field;
        messages.push(`${fieldName}: ${message}`);
    }
    return messages.join('\n');
}

// 로딩 상태 관리
function setLoadingState(isLoading) {
    if (isLoading) {
        calculateBtn.disabled = true;
        calculateBtn.textContent = '계산 중...';
        calculateBtn.style.opacity = '0.6';
    } else {
        calculateBtn.disabled = false;
        calculateBtn.textContent = '계산하기';
        calculateBtn.style.opacity = '1';
    }
}

// ========== 다안 비교 기능 ==========

let currentMode = '2'; // '2' 또는 'multi'
let multiOptionCount = 3;

// 모드 전환
document.getElementById('mode2Btn').addEventListener('click', () => {
    currentMode = '2';
    switchMode('2');
});

document.getElementById('modeMultiBtn').addEventListener('click', () => {
    currentMode = 'multi';
    switchMode('multi');
});

function switchMode(mode) {
    const twoInputSection = document.querySelector('.input-section:nth-of-type(2)');
    const multiInputSection = document.getElementById('multiInputSection');
    const mode2Btn = document.getElementById('mode2Btn');
    const modeMultiBtn = document.getElementById('modeMultiBtn');
    
    if (mode === '2') {
        twoInputSection.classList.remove('hidden');
        multiInputSection.classList.add('hidden');
        mode2Btn.classList.add('active');
        modeMultiBtn.classList.remove('active');
    } else {
        twoInputSection.classList.add('hidden');
        multiInputSection.classList.remove('hidden');
        mode2Btn.classList.remove('active');
        modeMultiBtn.classList.add('active');
        initMultiOptions();
    }
}

// 다안 비교 옵션 초기화
function initMultiOptions() {
    const container = document.getElementById('multiOptionsContainer');
    container.innerHTML = '';
    
    for (let i = 0; i < multiOptionCount; i++) {
        const optionCard = createMultiOptionCard(i);
        container.appendChild(optionCard);
    }
    
    updateMultiButtons();
}

function createMultiOptionCard(index) {
    const card = document.createElement('div');
    card.className = 'multi-option-card';
    card.innerHTML = `
        <h4>선택지 ${String.fromCharCode(65 + index)}</h4>
        <div class="input-group">
            <label>소요 시간 (분)</label>
            <input type="number" class="multi-time-input" data-index="${index}" min="0" placeholder="예: 10" required>
        </div>
        <div class="input-group">
            <label>직접 비용 (원)</label>
            <input type="number" class="multi-cost-input" data-index="${index}" min="0" placeholder="예: 3000" required>
        </div>
    `;
    return card;
}

// 다안 비교 옵션 추가/제거
document.getElementById('addOptionBtn').addEventListener('click', () => {
    if (multiOptionCount < 5) {
        multiOptionCount++;
        const container = document.getElementById('multiOptionsContainer');
        const optionCard = createMultiOptionCard(multiOptionCount - 1);
        container.appendChild(optionCard);
        updateMultiButtons();
    }
});

document.getElementById('removeOptionBtn').addEventListener('click', () => {
    if (multiOptionCount > 3) {
        multiOptionCount--;
        const container = document.getElementById('multiOptionsContainer');
        container.removeChild(container.lastChild);
        updateMultiButtons();
    }
});

function updateMultiButtons() {
    const addBtn = document.getElementById('addOptionBtn');
    const removeBtn = document.getElementById('removeOptionBtn');
    
    addBtn.disabled = multiOptionCount >= 5;
    removeBtn.classList.toggle('hidden', multiOptionCount <= 3);
}

// 다안 비교 계산
async function calculateMulti() {
    // 시급 검증
    const hourlyWageStr = hourlyWageInput.value.trim();
    if (!hourlyWageStr || hourlyWageStr === '') {
        showError('시급을 입력해주세요.');
        hourlyWageInput.focus();
        return;
    }
    
    const hourlyWage = parseInt(hourlyWageStr);
    if (isNaN(hourlyWage) || hourlyWage < 1) {
        showError('시급은 1원 이상의 숫자여야 합니다.');
        hourlyWageInput.focus();
        return;
    }
    
    // 상한선 체크 (1억원)
    if (hourlyWage > 100000000) {
        showError('시급은 1억원 이하로 입력해주세요.');
        hourlyWageInput.focus();
        return;
    }
    
    // 큰 값 경고
    if (hourlyWage >= 1000000) {
        if (!confirm(`입력하신 시급(${hourlyWage.toLocaleString()}원)이 매우 높습니다. 계속하시겠습니까?`)) {
            return;
        }
    }
    
    const options = [];
    
    for (let i = 0; i < multiOptionCount; i++) {
        const timeInput = document.querySelector(`.multi-time-input[data-index="${i}"]`);
        const costInput = document.querySelector(`.multi-cost-input[data-index="${i}"]`);
        
        if (!timeInput || !costInput) {
            showError(`선택지 ${String.fromCharCode(65 + i)} 입력 필드를 찾을 수 없습니다.`);
            return;
        }
        
        if (!timeInput.value.trim() || !costInput.value.trim()) {
            showError(`선택지 ${String.fromCharCode(65 + i)}의 모든 값을 입력해주세요.`);
            timeInput.focus();
            return;
        }
        
        const timeMinutes = parseInt(timeInput.value.trim());
        const directCost = parseInt(costInput.value.trim());
        
        if (isNaN(timeMinutes) || timeMinutes < 0) {
            showError(`선택지 ${String.fromCharCode(65 + i)}의 소요 시간은 0분 이상이어야 합니다.`);
            timeInput.focus();
            return;
        }
        
        if (isNaN(directCost) || directCost < 0) {
            showError(`선택지 ${String.fromCharCode(65 + i)}의 직접 비용은 0원 이상이어야 합니다.`);
            costInput.focus();
            return;
        }
        
        options.push({
            timeMinutes,
            directCost
        });
    }
    
    setLoadingState(true);
    
    try {
        const response = await fetch('/api/calculate/multi', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ hourlyWage, options })
        });
        
        if (!response.ok) {
            const errorData = await response.json();
            showError(formatValidationErrors(errorData));
            return;
        }
        
        const result = await response.json();
        displayMultiResult(result);
        saveToHistory('multi', { hourlyWage, options }, result);
        hideError();
    } catch (error) {
        showError('네트워크 오류가 발생했습니다: ' + error.message);
    } finally {
        setLoadingState(false);
    }
}

function displayMultiResult(result) {
    const resultSection = document.getElementById('resultSection');
    resultSection.innerHTML = `
        <h2>📊 다안 비교 결과</h2>
        ${result.recommendedOption ? 
            `<div class="recommendation-box">
                <div class="recommendation">추천: <span>${result.results.find(r => r.optionNumber === result.recommendedOption).optionName}</span></div>
                <div class="difference">최소 비용: ${result.minTotalCost.toLocaleString()}원 | 최대 비용: ${result.maxTotalCost.toLocaleString()}원</div>
                <div class="difference">최대 차액: ${result.maxDifference.toLocaleString()}원</div>
            </div>` :
            `<div class="recommendation-box">
                <div class="recommendation">모든 선택지의 총 비용이 동일합니다.</div>
            </div>`
        }
        <div class="multi-result-container">
            ${result.results.map(r => `
                <div class="multi-result-card ${r.optionNumber === result.recommendedOption ? 'recommended' : ''}">
                    <h4>${r.optionName}</h4>
                    <div class="cost-breakdown">
                        <div class="cost-item">
                            <span class="cost-label">직접 비용:</span>
                            <span class="cost-value">${r.breakdown.directCost.toLocaleString()}</span>원
                        </div>
                        <div class="cost-item">
                            <span class="cost-label">시간 비용:</span>
                            <span class="cost-value">${r.breakdown.timeCost.toLocaleString()}</span>원
                        </div>
                        <div class="cost-item total">
                            <span class="cost-label">총 비용:</span>
                            <span class="cost-value">${r.breakdown.totalCost.toLocaleString()}</span>원
                        </div>
                    </div>
                </div>
            `).join('')}
        </div>
        <div class="formula-section">
            <button class="formula-toggle" id="formulaToggle">📐 계산식 보기</button>
            <div class="formula-content hidden" id="formulaContent">
                <pre>${result.formula}</pre>
            </div>
        </div>
    `;
    
    resultSection.classList.remove('hidden');
    document.getElementById('formulaToggle').addEventListener('click', () => {
        const formulaContent = document.getElementById('formulaContent');
        const formulaToggle = document.getElementById('formulaToggle');
        formulaContent.classList.toggle('hidden');
        formulaToggle.textContent = formulaContent.classList.contains('hidden') ? '📐 계산식 보기' : '📐 계산식 숨기기';
    });
    
    resultSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

// 계산 버튼 클릭 이벤트 (모드에 따라 분기)
calculateBtn.addEventListener('click', async () => {
    if (currentMode === 'multi') {
        await calculateMulti();
    } else {
        await calculateTwoOptions();
    }
});

// ========== 히스토리 기능 ==========

const HISTORY_KEY = 'opportunityCostHistory';
const MAX_HISTORY = 20;

function saveToHistory(mode, request, result) {
    let history = getHistory();
    const historyItem = {
        id: Date.now(),
        mode,
        request,
        result,
        timestamp: new Date().toISOString()
    };
    history.unshift(historyItem);
    if (history.length > MAX_HISTORY) {
        history = history.slice(0, MAX_HISTORY);
    }
    // sessionStorage 사용: 탭 닫으면 자동 삭제
    sessionStorage.setItem(HISTORY_KEY, JSON.stringify(history));
}

function getHistory() {
    const stored = sessionStorage.getItem(HISTORY_KEY);
    return stored ? JSON.parse(stored) : [];
}

function clearHistory() {
    sessionStorage.removeItem(HISTORY_KEY);
    renderHistory();
}

function renderHistory() {
    const historyList = document.getElementById('historyList');
    const history = getHistory();
    
    if (history.length === 0) {
        historyList.innerHTML = '<p style="text-align: center; color: #666;">저장된 히스토리가 없습니다.</p>';
        return;
    }
    
    historyList.innerHTML = history.map(item => {
        const date = new Date(item.timestamp);
        const dateStr = date.toLocaleString('ko-KR');
        const summary = item.mode === 'multi' 
            ? `${item.request.options.length}개 선택지 비교`
            : `선택지 A vs B`;
        const recommendation = item.result.recommendation || 
            (item.result.recommendedOption ? `선택지 ${String.fromCharCode(64 + item.result.recommendedOption)}` : '동일');
        
        return `
            <div class="history-item" onclick="loadHistoryItem(${item.id})">
                <div class="history-item-header">
                    <span class="history-item-date">${dateStr}</span>
                    <span>${summary}</span>
                </div>
                <div class="history-item-summary">
                    시급: ${item.request.hourlyWage.toLocaleString()}원/시간 | 추천: ${recommendation}
                </div>
            </div>
        `;
    }).join('');
}

function loadHistoryItem(id) {
    const history = getHistory();
    const item = history.find(h => h.id === id);
    if (!item) return;
    
    hourlyWageInput.value = item.request.hourlyWage;
    hourlyWageInput.dispatchEvent(new Event('input'));
    
    if (item.mode === 'multi') {
        switchMode('multi');
        multiOptionCount = item.request.options.length;
        initMultiOptions();
        item.request.options.forEach((opt, idx) => {
            const timeInput = document.querySelector(`.multi-time-input[data-index="${idx}"]`);
            const costInput = document.querySelector(`.multi-cost-input[data-index="${idx}"]`);
            if (timeInput) timeInput.value = opt.timeMinutes;
            if (costInput) costInput.value = opt.directCost;
        });
    } else {
        switchMode('2');
        optionATimeInput.value = item.request.optionA.timeMinutes;
        optionACostInput.value = item.request.optionA.directCost;
        optionBTimeInput.value = item.request.optionB.timeMinutes;
        optionBCostInput.value = item.request.optionB.directCost;
    }
    
    // 결과 표시
    if (item.mode === 'multi') {
        displayMultiResult(item.result);
    } else {
        displayResult(item.result);
    }
}

document.getElementById('showHistoryBtn').addEventListener('click', () => {
    renderHistory();
});

document.getElementById('clearHistoryBtn').addEventListener('click', () => {
    if (confirm('히스토리를 모두 삭제하시겠습니까?')) {
        clearHistory();
    }
});

// 페이지 로드 시 히스토리 렌더링
window.addEventListener('load', () => {
    renderHistory();
});
