export type BinaryOperator = 'ADD' | 'SUBTRACT' | 'MULTIPLY' | 'DIVIDE';

export const OPERATOR_SYMBOLS: Record<BinaryOperator, string> = {
  ADD: '+',
  SUBTRACT: '−',
  MULTIPLY: '×',
  DIVIDE: '÷',
};

export type CalculatorError = 'DIVIDE_BY_ZERO' | 'INVALID_INPUT' | 'OVERFLOW';

export interface NumberFormatSymbols {
  decimalSeparator: string;
  groupingSeparator: string;
}

export interface BigDec {
  coeff: bigint;
  scale: number;
}

export interface HistoryEntry {
  id: number;
  expression: string;
  resultFormatted: string;
  rawValue: BigDec;
}

export interface CalculatorState {
  inputBuffer: string;
  isEnteringNumber: boolean;
  accumulator: BigDec | null;
  leftExpressionText: string | null;
  pendingOperator: BinaryOperator | null;
  lastBinaryOperator: BinaryOperator | null;
  lastRightOperand: BigDec | null;
  isResultDisplayed: boolean;
  expressionText: string;
  unaryExpressionOperand: string | null;
  memoryValue: BigDec | null;
  error: CalculatorError | null;
  history: HistoryEntry[];
  nextHistoryId: number;
  isHistoryVisible: boolean;
}

export const INITIAL_STATE: CalculatorState = {
  inputBuffer: '0',
  isEnteringNumber: false,
  accumulator: null,
  leftExpressionText: null,
  pendingOperator: null,
  lastBinaryOperator: null,
  lastRightOperand: null,
  isResultDisplayed: false,
  expressionText: '',
  unaryExpressionOperand: null,
  memoryValue: null,
  error: null,
  history: [],
  nextHistoryId: 1,
  isHistoryVisible: false,
};

const MAX_SIG_DIGITS = 16;
const INTERNAL_SIG_DIGITS = 24;
const MAX_EXP = 9999;
const MIN_EXP = -9999;
const SCI_LOW_EXP = -10;
const ZERO: BigDec = { coeff: 0n, scale: 0 };

function pow10(n: number): bigint {
  if (n <= 0) return 1n;
  return 10n ** BigInt(n);
}

function absBig(x: bigint): bigint {
  return x < 0n ? -x : x;
}

function stripTrailing(d: BigDec): BigDec {
  if (d.coeff === 0n) return ZERO;
  let coeff = d.coeff;
  let scale = d.scale;
  while (scale > 0 && coeff % 10n === 0n) {
    coeff /= 10n;
    scale -= 1;
  }
  while (scale < 0) {
    coeff *= 10n;
    scale += 1;
  }
  return { coeff, scale };
}

function digitCount(n: bigint): number {
  const a = absBig(n);
  if (a === 0n) return 1;
  return a.toString().length;
}

function sciExponent(d: BigDec): number {
  if (d.coeff === 0n) return 0;
  const s = stripTrailing(d);
  return digitCount(s.coeff) - s.scale - 1;
}

function isOverflow(d: BigDec): boolean {
  if (d.coeff === 0n) return false;
  const exp = sciExponent(d);
  return exp > MAX_EXP || exp < MIN_EXP;
}

function roundToSig(d: BigDec, sigDigits: number): BigDec {
  if (d.coeff === 0n) return ZERO;
  const neg = d.coeff < 0n;
  let a = absBig(d.coeff);
  const digits = a.toString().length;
  if (digits <= sigDigits) {
    return stripTrailing({ coeff: neg ? -a : a, scale: d.scale });
  }
  const drop = digits - sigDigits;
  const divisor = pow10(drop);
  const quotient = a / divisor;
  const rem = a % divisor;
  const half = divisor / 2n;
  const rounded = rem >= half ? quotient + 1n : quotient;
  const newScale = d.scale - drop;
  return stripTrailing({ coeff: neg ? -rounded : rounded, scale: newScale });
}

function normalize(d: BigDec): BigDec {
  return roundToSig(d, MAX_SIG_DIGITS);
}

function parseBigDec(raw: string): BigDec {
  const cleaned = raw.endsWith('.') ? raw.slice(0, -1) : raw;
  if (!cleaned || cleaned === '-') return ZERO;
  const neg = cleaned.startsWith('-');
  const unsigned = neg ? cleaned.slice(1) : cleaned;
  const dot = unsigned.indexOf('.');
  if (dot === -1) {
    const c = BigInt(unsigned || '0');
    return stripTrailing({ coeff: neg ? -c : c, scale: 0 });
  }
  const intPart = unsigned.slice(0, dot) || '0';
  const fracPart = unsigned.slice(dot + 1);
  const c = BigInt(intPart + fracPart);
  return stripTrailing({ coeff: neg ? -c : c, scale: fracPart.length });
}

function toPlainString(d: BigDec): string {
  const s = stripTrailing(d);
  if (s.coeff === 0n) return '0';
  const neg = s.coeff < 0n;
  const digits = absBig(s.coeff).toString();
  if (s.scale === 0) return (neg ? '-' : '') + digits;
  if (s.scale >= digits.length) {
    const zeros = '0'.repeat(s.scale - digits.length);
    return `${neg ? '-' : ''}0.${zeros}${digits}`;
  }
  const split = digits.length - s.scale;
  return `${neg ? '-' : ''}${digits.slice(0, split)}.${digits.slice(split)}`;
}

function addDec(a: BigDec, b: BigDec): BigDec {
  const targetScale = Math.max(a.scale, b.scale);
  const ac = a.coeff * pow10(targetScale - a.scale);
  const bc = b.coeff * pow10(targetScale - b.scale);
  return roundToSig({ coeff: ac + bc, scale: targetScale }, INTERNAL_SIG_DIGITS);
}

function subDec(a: BigDec, b: BigDec): BigDec {
  return addDec(a, { coeff: -b.coeff, scale: b.scale });
}

function mulDec(a: BigDec, b: BigDec): BigDec {
  return roundToSig({ coeff: a.coeff * b.coeff, scale: a.scale + b.scale }, INTERNAL_SIG_DIGITS);
}

function divDec(a: BigDec, b: BigDec): BigDec {
  if (a.coeff === 0n) return ZERO;
  const neg = (a.coeff < 0n) !== (b.coeff < 0n);
  const ac = absBig(a.coeff);
  const bc = absBig(b.coeff);
  const extraShift = INTERNAL_SIG_DIGITS + digitCount(bc) + 2;
  const scaledNum = ac * pow10(extraShift);
  const q = scaledNum / bc;
  const r = scaledNum % bc;
  const roundedQ = r * 2n >= bc ? q + 1n : q;
  const resultScale = a.scale - b.scale + extraShift;
  return roundToSig({ coeff: neg ? -roundedQ : roundedQ, scale: resultScale }, INTERNAL_SIG_DIGITS);
}

function sqrtDec(a: BigDec): BigDec {
  if (a.coeff === 0n) return ZERO;
  const s = stripTrailing(a);
  const exp = digitCount(s.coeff) - s.scale - 1;
  const fracScale = Math.max(INTERNAL_SIG_DIGITS - Math.floor(exp / 2) + 4, 24);
  const shift = 2 * fracScale - s.scale;
  const target = shift >= 0 ? s.coeff * pow10(shift) : s.coeff / pow10(-shift);
  if (target === 0n) return ZERO;

  let x = pow10(Math.floor(target.toString().length / 2) + 1);
  for (let i = 0; i < 120; i++) {
    const next = (x + target / x) / 2n;
    if (next === x || next === x - 1n) {
      x = next * next <= target ? next : x;
      break;
    }
    x = next;
  }
  return roundToSig({ coeff: x, scale: fracScale }, INTERNAL_SIG_DIGITS);
}

function groupInt(digits: string, grouping: string): string {
  if (digits.length <= 3 || !grouping) return digits;
  let out = '';
  let count = 0;
  for (let i = digits.length - 1; i >= 0; i--) {
    if (count > 0 && count % 3 === 0) out = grouping + out;
    out = digits[i] + out;
    count++;
  }
  return out;
}

export function formatInputBuffer(buffer: string, symbols: NumberFormatSymbols): string {
  if (!buffer) return '0';
  const neg = buffer.startsWith('-');
  const unsigned = neg ? buffer.slice(1) : buffer;
  const dot = unsigned.indexOf('.');
  const rawInt = dot >= 0 ? unsigned.slice(0, dot) : unsigned;
  const grouped = groupInt(rawInt || '0', symbols.groupingSeparator);
  const sign = neg ? '-' : '';
  if (dot >= 0) {
    return `${sign}${grouped}${symbols.decimalSeparator}${unsigned.slice(dot + 1)}`;
  }
  return `${sign}${grouped}`;
}

function formatSci(d: BigDec, exp: number, symbols: NumberFormatSymbols): string {
  const neg = d.coeff < 0n;
  const digits = absBig(d.coeff).toString().replace(/0+$/, '') || '0';
  const mantissa = digits.length === 1 ? digits : `${digits[0]}${symbols.decimalSeparator}${digits.slice(1)}`;
  return `${neg ? '-' : ''}${mantissa}e${exp >= 0 ? '+' : ''}${exp}`;
}

export function formatResult(d: BigDec, symbols: NumberFormatSymbols): string {
  const norm = normalize(d);
  if (norm.coeff === 0n) return '0';
  const exp = sciExponent(norm);
  if (exp >= MAX_SIG_DIGITS || exp < SCI_LOW_EXP) {
    return formatSci(norm, exp, symbols);
  }
  const plain = toPlainString(norm);
  const neg = plain.startsWith('-');
  const unsigned = neg ? plain.slice(1) : plain;
  const dot = unsigned.indexOf('.');
  const intPart = dot >= 0 ? unsigned.slice(0, dot) : unsigned;
  const grouped = groupInt(intPart, symbols.groupingSeparator);
  const sign = neg ? '-' : '';
  return dot >= 0
    ? `${sign}${grouped}${symbols.decimalSeparator}${unsigned.slice(dot + 1)}`
    : `${sign}${grouped}`;
}

export function formatForExpression(d: BigDec, symbols: NumberFormatSymbols): string {
  const norm = normalize(d);
  if (norm.coeff === 0n) return '0';
  const exp = sciExponent(norm);
  if (exp >= MAX_SIG_DIGITS || exp < SCI_LOW_EXP) {
    return formatSci(norm, exp, symbols);
  }
  return toPlainString(norm).replace('.', symbols.decimalSeparator);
}

export function formatDisplay(state: CalculatorState, symbols: NumberFormatSymbols): string {
  if (state.isEnteringNumber) {
    return formatInputBuffer(state.inputBuffer, symbols);
  }
  return formatResult(parseBigDec(state.inputBuffer), symbols);
}

export type Action =
  | { type: 'DIGIT'; digit: string }
  | { type: 'DECIMAL' }
  | { type: 'BACKSPACE' }
  | { type: 'CLEAR_ENTRY' }
  | { type: 'CLEAR_ALL' }
  | { type: 'NEGATE' }
  | { type: 'OPERATOR'; operator: BinaryOperator }
  | { type: 'EQUALS' }
  | { type: 'PERCENT' }
  | { type: 'RECIPROCAL' }
  | { type: 'SQUARE' }
  | { type: 'SQRT' }
  | { type: 'MC' }
  | { type: 'MR' }
  | { type: 'M_ADD' }
  | { type: 'M_SUB' }
  | { type: 'MS' }
  | { type: 'TOGGLE_HISTORY' }
  | { type: 'CLOSE_HISTORY' }
  | { type: 'CLEAR_HISTORY' }
  | { type: 'RECALL_HISTORY'; entry: HistoryEntry };

function clearCalcKeepMeta(state: CalculatorState): CalculatorState {
  return {
    ...state,
    inputBuffer: '0',
    isEnteringNumber: false,
    accumulator: null,
    leftExpressionText: null,
    pendingOperator: null,
    lastBinaryOperator: null,
    lastRightOperand: null,
    isResultDisplayed: false,
    expressionText: '',
    unaryExpressionOperand: null,
    error: null,
  };
}

function evalBinary(left: BigDec, op: BinaryOperator, right: BigDec): { ok: true; value: BigDec } | { ok: false; error: CalculatorError } {
  let raw: BigDec;
  switch (op) {
    case 'ADD':
      raw = addDec(left, right);
      break;
    case 'SUBTRACT':
      raw = subDec(left, right);
      break;
    case 'MULTIPLY':
      raw = mulDec(left, right);
      break;
    case 'DIVIDE':
      if (right.coeff === 0n) return { ok: false, error: 'DIVIDE_BY_ZERO' };
      raw = divDec(left, right);
      break;
  }
  if (isOverflow(raw)) return { ok: false, error: 'OVERFLOW' };
  return { ok: true, value: normalize(raw) };
}

export function reduceCalculator(
  state: CalculatorState,
  action: Action,
  symbols: NumberFormatSymbols
): CalculatorState {
  switch (action.type) {
    case 'DIGIT': {
      const base = state.error ? clearCalcKeepMeta(state) : state;
      if (base.isResultDisplayed && !base.pendingOperator) {
        return {
          ...base,
          inputBuffer: action.digit,
          isEnteringNumber: true,
          lastBinaryOperator: null,
          lastRightOperand: null,
          isResultDisplayed: false,
          expressionText: '',
          unaryExpressionOperand: null,
        };
      }
      if (!base.isEnteringNumber) {
        return {
          ...base,
          inputBuffer: action.digit,
          isEnteringNumber: true,
          isResultDisplayed: false,
          unaryExpressionOperand: null,
        };
      }
      const digits = base.inputBuffer.replace(/[^0-9]/g, '').length;
      if (digits >= MAX_SIG_DIGITS) return base;
      const updated =
        base.inputBuffer === '0'
          ? action.digit
          : base.inputBuffer === '-0'
          ? `-${action.digit}`
          : base.inputBuffer + action.digit;
      return { ...base, inputBuffer: updated };
    }

    case 'DECIMAL': {
      const base = state.error ? clearCalcKeepMeta(state) : state;
      if (base.isResultDisplayed && !base.pendingOperator) {
        return {
          ...base,
          inputBuffer: '0.',
          isEnteringNumber: true,
          lastBinaryOperator: null,
          lastRightOperand: null,
          isResultDisplayed: false,
          expressionText: '',
          unaryExpressionOperand: null,
        };
      }
      if (!base.isEnteringNumber) {
        return {
          ...base,
          inputBuffer: '0.',
          isEnteringNumber: true,
          isResultDisplayed: false,
          unaryExpressionOperand: null,
        };
      }
      if (base.inputBuffer.includes('.')) return base;
      return { ...base, inputBuffer: `${base.inputBuffer}.` };
    }

    case 'BACKSPACE': {
      if (state.error) return clearCalcKeepMeta(state);
      if (!state.isEnteringNumber) {
        return state.isResultDisplayed && !state.pendingOperator
          ? { ...state, expressionText: '', unaryExpressionOperand: null }
          : state;
      }
      const dropped = state.inputBuffer.slice(0, -1);
      const norm = !dropped || dropped === '-' || dropped === '-0' ? '0' : dropped;
      return { ...state, inputBuffer: norm };
    }

    case 'CLEAR_ENTRY': {
      if (state.error || (state.isResultDisplayed && !state.pendingOperator)) {
        return clearCalcKeepMeta(state);
      }
      const restored =
        state.pendingOperator && state.leftExpressionText
          ? `${state.leftExpressionText} ${OPERATOR_SYMBOLS[state.pendingOperator]}`
          : '';
      return {
        ...state,
        inputBuffer: '0',
        isEnteringNumber: false,
        unaryExpressionOperand: null,
        expressionText: restored,
      };
    }

    case 'CLEAR_ALL':
      return clearCalcKeepMeta(state);

    case 'NEGATE': {
      if (state.error) return state;
      const cur = parseBigDec(state.inputBuffer);
      if (cur.coeff === 0n && !state.inputBuffer.includes('.')) {
        return { ...state, inputBuffer: '0' };
      }
      if (state.isEnteringNumber) {
        return {
          ...state,
          inputBuffer: state.inputBuffer.startsWith('-')
            ? state.inputBuffer.slice(1)
            : `-${state.inputBuffer}`,
        };
      }
      const neg = normalize({ coeff: -cur.coeff, scale: cur.scale });
      const newBuf = toPlainString(neg);
      const newUnary =
        state.unaryExpressionOperand || state.isResultDisplayed
          ? `negate( ${state.unaryExpressionOperand ?? formatForExpression(cur, symbols)} )`
          : null;
      let newExpr = state.expressionText;
      if (state.pendingOperator && state.leftExpressionText && newUnary) {
        newExpr = `${state.leftExpressionText} ${OPERATOR_SYMBOLS[state.pendingOperator]} ${newUnary}`;
      } else if (!state.pendingOperator && newUnary) {
        newExpr = newUnary;
      }
      return {
        ...state,
        inputBuffer: newBuf,
        unaryExpressionOperand: newUnary,
        expressionText: newExpr,
      };
    }

    case 'OPERATOR': {
      if (state.error) return state;
      const opSym = OPERATOR_SYMBOLS[action.operator];
      const hasFresh = state.isEnteringNumber || Boolean(state.unaryExpressionOperand) || state.isResultDisplayed;

      if (state.pendingOperator && state.accumulator) {
        if (!hasFresh) {
          const leftExpr = state.leftExpressionText ?? formatForExpression(state.accumulator, symbols);
          return {
            ...state,
            pendingOperator: action.operator,
            leftExpressionText: leftExpr,
            expressionText: `${leftExpr} ${opSym}`,
          };
        }
        const right = parseBigDec(state.inputBuffer);
        const res = evalBinary(state.accumulator, state.pendingOperator, right);
        if (!res.ok) {
          return {
            ...state,
            error: res.error,
            accumulator: null,
            pendingOperator: null,
            leftExpressionText: null,
            isEnteringNumber: false,
            isResultDisplayed: false,
            unaryExpressionOperand: null,
          };
        }
        const leftExpr = formatForExpression(res.value, symbols);
        return {
          ...state,
          inputBuffer: toPlainString(res.value),
          isEnteringNumber: false,
          accumulator: res.value,
          leftExpressionText: leftExpr,
          pendingOperator: action.operator,
          lastBinaryOperator: null,
          lastRightOperand: null,
          isResultDisplayed: false,
          expressionText: `${leftExpr} ${opSym}`,
          unaryExpressionOperand: null,
        };
      }

      const left = parseBigDec(state.inputBuffer);
      const leftExpr = state.unaryExpressionOperand ?? formatForExpression(left, symbols);
      return {
        ...state,
        accumulator: left,
        leftExpressionText: leftExpr,
        pendingOperator: action.operator,
        lastBinaryOperator: null,
        lastRightOperand: null,
        isEnteringNumber: false,
        isResultDisplayed: false,
        expressionText: `${leftExpr} ${opSym}`,
        unaryExpressionOperand: null,
      };
    }

    case 'EQUALS': {
      if (state.error) return clearCalcKeepMeta(state);

      if (state.pendingOperator && state.accumulator) {
        const left = state.accumulator;
        const right = parseBigDec(state.inputBuffer);
        const leftExpr = state.leftExpressionText ?? formatForExpression(left, symbols);
        const rightExpr = state.unaryExpressionOperand ?? formatForExpression(right, symbols);
        const fullExpr = `${leftExpr} ${OPERATOR_SYMBOLS[state.pendingOperator]} ${rightExpr} =`;
        const res = evalBinary(left, state.pendingOperator, right);
        if (!res.ok) {
          return {
            ...state,
            error: res.error,
            accumulator: null,
            leftExpressionText: null,
            pendingOperator: null,
            lastBinaryOperator: null,
            lastRightOperand: null,
            isEnteringNumber: false,
            isResultDisplayed: false,
            expressionText: fullExpr,
            unaryExpressionOperand: null,
          };
        }
        const formatted = formatResult(res.value, symbols);
        const entry: HistoryEntry = {
          id: state.nextHistoryId,
          expression: fullExpr,
          resultFormatted: formatted,
          rawValue: res.value,
        };
        return {
          ...state,
          inputBuffer: toPlainString(res.value),
          isEnteringNumber: false,
          accumulator: null,
          leftExpressionText: null,
          pendingOperator: null,
          lastBinaryOperator: state.pendingOperator,
          lastRightOperand: right,
          isResultDisplayed: true,
          expressionText: fullExpr,
          unaryExpressionOperand: null,
          history: [entry, ...state.history],
          nextHistoryId: state.nextHistoryId + 1,
        };
      }

      if (state.lastBinaryOperator && state.lastRightOperand) {
        const left = parseBigDec(state.inputBuffer);
        const right = state.lastRightOperand;
        const op = state.lastBinaryOperator;
        const fullExpr = `${formatForExpression(left, symbols)} ${OPERATOR_SYMBOLS[op]} ${formatForExpression(right, symbols)} =`;
        const res = evalBinary(left, op, right);
        if (!res.ok) {
          return {
            ...state,
            error: res.error,
            accumulator: null,
            leftExpressionText: null,
            pendingOperator: null,
            lastBinaryOperator: null,
            lastRightOperand: null,
            isEnteringNumber: false,
            isResultDisplayed: false,
            expressionText: fullExpr,
            unaryExpressionOperand: null,
          };
        }
        const formatted = formatResult(res.value, symbols);
        const entry: HistoryEntry = {
          id: state.nextHistoryId,
          expression: fullExpr,
          resultFormatted: formatted,
          rawValue: res.value,
        };
        return {
          ...state,
          inputBuffer: toPlainString(res.value),
          isEnteringNumber: false,
          isResultDisplayed: true,
          expressionText: fullExpr,
          unaryExpressionOperand: null,
          history: [entry, ...state.history],
          nextHistoryId: state.nextHistoryId + 1,
        };
      }

      const cur = parseBigDec(state.inputBuffer);
      const expr = `${state.unaryExpressionOperand ?? formatForExpression(cur, symbols)} =`;
      const norm = normalize(cur);
      const entry: HistoryEntry = {
        id: state.nextHistoryId,
        expression: expr,
        resultFormatted: formatResult(norm, symbols),
        rawValue: norm,
      };
      return {
        ...state,
        inputBuffer: toPlainString(norm),
        isEnteringNumber: false,
        isResultDisplayed: true,
        expressionText: expr,
        unaryExpressionOperand: null,
        history: [entry, ...state.history],
        nextHistoryId: state.nextHistoryId + 1,
      };
    }

    case 'PERCENT': {
      if (state.error) return state;
      if (!state.pendingOperator || !state.accumulator) {
        return {
          ...state,
          inputBuffer: '0',
          isEnteringNumber: false,
          isResultDisplayed: true,
          expressionText: '0',
          unaryExpressionOperand: null,
        };
      }
      const cur = parseBigDec(state.inputBuffer);
      const hundred: BigDec = { coeff: 100n, scale: 0 };
      const raw =
        state.pendingOperator === 'ADD' || state.pendingOperator === 'SUBTRACT'
          ? divDec(mulDec(state.accumulator, cur), hundred)
          : divDec(cur, hundred);
      if (isOverflow(raw)) {
        return {
          ...state,
          error: 'OVERFLOW',
          accumulator: null,
          leftExpressionText: null,
          pendingOperator: null,
          isEnteringNumber: false,
          isResultDisplayed: false,
          unaryExpressionOperand: null,
        };
      }
      const norm = normalize(raw);
      const leftExpr = state.leftExpressionText ?? formatForExpression(state.accumulator, symbols);
      const rightExpr = formatForExpression(norm, symbols);
      return {
        ...state,
        inputBuffer: toPlainString(norm),
        isEnteringNumber: false,
        isResultDisplayed: true,
        unaryExpressionOperand: rightExpr,
        expressionText: `${leftExpr} ${OPERATOR_SYMBOLS[state.pendingOperator]} ${rightExpr}`,
      };
    }

    case 'RECIPROCAL':
    case 'SQUARE':
    case 'SQRT': {
      if (state.error) return state;
      const operand = parseBigDec(state.inputBuffer);
      const inner = state.unaryExpressionOperand ?? formatForExpression(operand, symbols);
      const newUnary =
        action.type === 'RECIPROCAL'
          ? `1/( ${inner} )`
          : action.type === 'SQUARE'
          ? `sqr( ${inner} )`
          : `√( ${inner} )`;
      const composed =
        state.pendingOperator && state.leftExpressionText
          ? `${state.leftExpressionText} ${OPERATOR_SYMBOLS[state.pendingOperator]} ${newUnary}`
          : newUnary;

      let err: CalculatorError | null = null;
      let raw: BigDec = ZERO;
      if (action.type === 'RECIPROCAL') {
        if (operand.coeff === 0n) err = 'DIVIDE_BY_ZERO';
        else raw = divDec({ coeff: 1n, scale: 0 }, operand);
      } else if (action.type === 'SQUARE') {
        raw = mulDec(operand, operand);
      } else {
        if (operand.coeff < 0n) err = 'INVALID_INPUT';
        else raw = sqrtDec(operand);
      }

      if (!err && isOverflow(raw)) err = 'OVERFLOW';
      if (err) {
        return {
          ...state,
          error: err,
          accumulator: null,
          leftExpressionText: null,
          pendingOperator: null,
          lastBinaryOperator: null,
          lastRightOperand: null,
          isEnteringNumber: false,
          isResultDisplayed: false,
          expressionText: composed,
          unaryExpressionOperand: null,
        };
      }

      const norm = normalize(raw);
      if (state.pendingOperator) {
        return {
          ...state,
          inputBuffer: toPlainString(norm),
          isEnteringNumber: false,
          isResultDisplayed: true,
          unaryExpressionOperand: newUnary,
          expressionText: composed,
        };
      }

      const entry: HistoryEntry = {
        id: state.nextHistoryId,
        expression: newUnary,
        resultFormatted: formatResult(norm, symbols),
        rawValue: norm,
      };
      return {
        ...state,
        inputBuffer: toPlainString(norm),
        isEnteringNumber: false,
        isResultDisplayed: true,
        unaryExpressionOperand: newUnary,
        expressionText: newUnary,
        lastBinaryOperator: null,
        lastRightOperand: null,
        history: [entry, ...state.history],
        nextHistoryId: state.nextHistoryId + 1,
      };
    }

    case 'MC':
      return { ...state, memoryValue: null };

    case 'MR': {
      if (state.error || !state.memoryValue) return state;
      return {
        ...state,
        inputBuffer: toPlainString(state.memoryValue),
        isEnteringNumber: false,
        isResultDisplayed: true,
        unaryExpressionOperand: null,
        expressionText: state.pendingOperator ? state.expressionText : '',
      };
    }

    case 'MS': {
      if (state.error) return state;
      return {
        ...state,
        memoryValue: normalize(parseBigDec(state.inputBuffer)),
        isEnteringNumber: false,
        isResultDisplayed: true,
      };
    }

    case 'M_ADD':
    case 'M_SUB': {
      if (state.error) return state;
      const cur = parseBigDec(state.inputBuffer);
      const existing = state.memoryValue ?? ZERO;
      const updated = action.type === 'M_ADD' ? addDec(existing, cur) : subDec(existing, cur);
      if (isOverflow(updated)) return { ...state, error: 'OVERFLOW' };
      return {
        ...state,
        memoryValue: normalize(updated),
        isEnteringNumber: false,
        isResultDisplayed: true,
      };
    }

    case 'TOGGLE_HISTORY':
      return { ...state, isHistoryVisible: !state.isHistoryVisible };

    case 'CLOSE_HISTORY':
      return { ...state, isHistoryVisible: false };

    case 'CLEAR_HISTORY':
      return { ...state, history: [] };

    case 'RECALL_HISTORY':
      return {
        ...state,
        inputBuffer: toPlainString(action.entry.rawValue),
        isEnteringNumber: false,
        accumulator: null,
        leftExpressionText: null,
        pendingOperator: null,
        lastBinaryOperator: null,
        lastRightOperand: null,
        isResultDisplayed: true,
        expressionText: action.entry.expression,
        unaryExpressionOperand: null,
        error: null,
        isHistoryVisible: false,
      };
  }
}
