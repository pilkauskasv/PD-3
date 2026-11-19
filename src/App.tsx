import { useEffect, useState } from 'react';
import { Delete, History, Moon, RotateCw, Sun, Trash2 } from 'lucide-react';
import {
  Action,
  CalculatorState,
  INITIAL_STATE,
  NumberFormatSymbols,
  formatDisplay,
  reduceCalculator,
} from './engine/decimalEngine';

type Lang = 'en' | 'lt';

const STRINGS: Record<
  Lang,
  {
    standard: string;
    decimal: string;
    grouping: string;
    emptyHistory: string;
    errZero: string;
    errInvalid: string;
    errOverflow: string;
  }
> = {
  en: {
    standard: 'Standard',
    decimal: '.',
    grouping: ',',
    emptyHistory: "There's no history yet.",
    errZero: 'Cannot divide by zero',
    errInvalid: 'Invalid input',
    errOverflow: 'Overflow',
  },
  lt: {
    standard: 'Standartinis',
    decimal: ',',
    grouping: '\u00A0',
    emptyHistory: 'Istorijos dar nėra.',
    errZero: 'Dalyba iš nulio negalima',
    errInvalid: 'Neteisinga įvestis',
    errOverflow: 'Perpildymas',
  },
};

export default function App() {
  const [state, setState] = useState<CalculatorState>(INITIAL_STATE);
  const [lang, setLang] = useState<Lang>('en');
  const [dark, setDark] = useState(false);
  const [landscape, setLandscape] = useState(false);

  const t = STRINGS[lang];
  const symbols: NumberFormatSymbols = {
    decimalSeparator: t.decimal,
    groupingSeparator: t.grouping,
  };

  const dispatch = (action: Action) => {
    setState((prev) => reduceCalculator(prev, action, symbols));
  };

  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key >= '0' && e.key <= '9') dispatch({ type: 'DIGIT', digit: e.key });
      else if (e.key === '.' || e.key === ',') dispatch({ type: 'DECIMAL' });
      else if (e.key === 'Backspace') dispatch({ type: 'BACKSPACE' });
      else if (e.key === 'Escape') {
        if (state.isHistoryVisible) dispatch({ type: 'CLOSE_HISTORY' });
        else dispatch({ type: 'CLEAR_ALL' });
      } else if (e.key === 'Enter' || e.key === '=') {
        e.preventDefault();
        dispatch({ type: 'EQUALS' });
      } else if (e.key === '+') dispatch({ type: 'OPERATOR', operator: 'ADD' });
      else if (e.key === '-') dispatch({ type: 'OPERATOR', operator: 'SUBTRACT' });
      else if (e.key === '*') dispatch({ type: 'OPERATOR', operator: 'MULTIPLY' });
      else if (e.key === '/') {
        e.preventDefault();
        dispatch({ type: 'OPERATOR', operator: 'DIVIDE' });
      } else if (e.key === '%') dispatch({ type: 'PERCENT' });
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  });

  const isInError = state.error !== null;
  const hasMemory = state.memoryValue !== null;
  const displayValue =
    state.error === 'DIVIDE_BY_ZERO'
      ? t.errZero
      : state.error === 'INVALID_INPUT'
      ? t.errInvalid
      : state.error === 'OVERFLOW'
      ? t.errOverflow
      : formatDisplay(state, symbols);

  const displayLen = displayValue.length;
  const displaySizeClass =
    displayLen > 18 ? 'text-[22px]' : displayLen > 14 ? 'text-[28px]' : displayLen > 10 ? 'text-[36px]' : 'text-[46px]';

  const bgMain = dark ? 'bg-[#202020] text-white' : 'bg-[#F3F3F3] text-[#1A1A1A]';
  const textSecondary = dark ? 'text-[#C5C5C5]' : 'text-[#5C5C5C]';
  const textDisabled = dark ? 'text-[#6B6B6B]' : 'text-[#A6A6A6]';
  const digitKey = dark
    ? 'bg-[#3B3B3B] hover:bg-[#353535] active:bg-[#323232] border-[#2D2D2D] text-white'
    : 'bg-white hover:bg-[#FAFAFA] active:bg-[#F5F5F5] border-[#E5E5E5] text-[#1A1A1A]';
  const funcKey = dark
    ? 'bg-[#323232] hover:bg-[#383838] active:bg-[#2B2B2B] border-[#2D2D2D] text-white'
    : 'bg-[#F9F9F9] hover:bg-[#F3F3F3] active:bg-[#EDEDED] border-[#E5E5E5] text-[#1A1A1A]';
  const equalsKey = dark
    ? 'bg-[#4CC2FF] hover:bg-[#47B7F0] active:bg-[#42B0E8] text-black border-transparent'
    : 'bg-[#0067C0] hover:bg-[#1975C5] active:bg-[#156AB5] text-white border-transparent';

  const renderKeypad = () => (
    <div className="grid grid-cols-4 grid-rows-6 gap-1 p-1 h-full">
      <button
        type="button"
        disabled={isInError}
        onClick={() => dispatch({ type: 'PERCENT' })}
        className={`rounded-[6px] border text-[17px] transition-colors disabled:opacity-45 ${funcKey}`}
      >
        %
      </button>
      <button
        type="button"
        onClick={() => dispatch({ type: 'CLEAR_ENTRY' })}
        className={`rounded-[6px] border text-[15px] transition-colors ${funcKey}`}
      >
        CE
      </button>
      <button
        type="button"
        onClick={() => dispatch({ type: 'CLEAR_ALL' })}
        className={`rounded-[6px] border text-[15px] transition-colors ${funcKey}`}
      >
        C
      </button>
      <button
        type="button"
        aria-label="Backspace"
        onClick={() => dispatch({ type: 'BACKSPACE' })}
        className={`rounded-[6px] border flex items-center justify-center transition-colors ${funcKey}`}
      >
        <Delete className="w-[18px] h-[18px]" strokeWidth={1.7} />
      </button>

      <button
        type="button"
        disabled={isInError}
        onClick={() => dispatch({ type: 'RECIPROCAL' })}
        className={`rounded-[6px] border text-[16px] transition-colors disabled:opacity-45 ${funcKey}`}
      >
        1/x
      </button>
      <button
        type="button"
        disabled={isInError}
        onClick={() => dispatch({ type: 'SQUARE' })}
        className={`rounded-[6px] border text-[16px] transition-colors disabled:opacity-45 ${funcKey}`}
      >
        x²
      </button>
      <button
        type="button"
        disabled={isInError}
        onClick={() => dispatch({ type: 'SQRT' })}
        className={`rounded-[6px] border text-[16px] transition-colors disabled:opacity-45 ${funcKey}`}
      >
        ²√x
      </button>
      <button
        type="button"
        disabled={isInError}
        onClick={() => dispatch({ type: 'OPERATOR', operator: 'DIVIDE' })}
        className={`rounded-[6px] border text-[21px] transition-colors disabled:opacity-45 ${funcKey}`}
      >
        ÷
      </button>

      {['7', '8', '9'].map((d) => (
        <button
          key={d}
          type="button"
          onClick={() => dispatch({ type: 'DIGIT', digit: d })}
          className={`rounded-[6px] border text-[20px] font-medium transition-colors ${digitKey}`}
        >
          {d}
        </button>
      ))}
      <button
        type="button"
        disabled={isInError}
        onClick={() => dispatch({ type: 'OPERATOR', operator: 'MULTIPLY' })}
        className={`rounded-[6px] border text-[21px] transition-colors disabled:opacity-45 ${funcKey}`}
      >
        ×
      </button>

      {['4', '5', '6'].map((d) => (
        <button
          key={d}
          type="button"
          onClick={() => dispatch({ type: 'DIGIT', digit: d })}
          className={`rounded-[6px] border text-[20px] font-medium transition-colors ${digitKey}`}
        >
          {d}
        </button>
      ))}
      <button
        type="button"
        disabled={isInError}
        onClick={() => dispatch({ type: 'OPERATOR', operator: 'SUBTRACT' })}
        className={`rounded-[6px] border text-[21px] transition-colors disabled:opacity-45 ${funcKey}`}
      >
        −
      </button>

      {['1', '2', '3'].map((d) => (
        <button
          key={d}
          type="button"
          onClick={() => dispatch({ type: 'DIGIT', digit: d })}
          className={`rounded-[6px] border text-[20px] font-medium transition-colors ${digitKey}`}
        >
          {d}
        </button>
      ))}
      <button
        type="button"
        disabled={isInError}
        onClick={() => dispatch({ type: 'OPERATOR', operator: 'ADD' })}
        className={`rounded-[6px] border text-[21px] transition-colors disabled:opacity-45 ${funcKey}`}
      >
        +
      </button>

      <button
        type="button"
        disabled={isInError}
        onClick={() => dispatch({ type: 'NEGATE' })}
        className={`rounded-[6px] border text-[17px] transition-colors disabled:opacity-45 ${digitKey}`}
      >
        +/−
      </button>
      <button
        type="button"
        onClick={() => dispatch({ type: 'DIGIT', digit: '0' })}
        className={`rounded-[6px] border text-[20px] font-medium transition-colors ${digitKey}`}
      >
        0
      </button>
      <button
        type="button"
        onClick={() => dispatch({ type: 'DECIMAL' })}
        className={`rounded-[6px] border text-[20px] font-semibold transition-colors ${digitKey}`}
      >
        {t.decimal}
      </button>
      <button
        type="button"
        onClick={() => dispatch({ type: 'EQUALS' })}
        className={`rounded-[6px] border text-[22px] transition-colors ${equalsKey}`}
      >
        =
      </button>
    </div>
  );

  const renderMemoryRow = () => (
    <div className="grid grid-cols-6 px-1 py-0.5 text-[12px] font-medium">
      <button
        type="button"
        disabled={!hasMemory || isInError}
        onClick={() => dispatch({ type: 'MC' })}
        className={`h-9 rounded-[6px] ${!hasMemory || isInError ? textDisabled : ''}`}
      >
        MC
      </button>
      <button
        type="button"
        disabled={!hasMemory || isInError}
        onClick={() => dispatch({ type: 'MR' })}
        className={`h-9 rounded-[6px] ${!hasMemory || isInError ? textDisabled : ''}`}
      >
        MR
      </button>
      <button
        type="button"
        disabled={isInError}
        onClick={() => dispatch({ type: 'M_ADD' })}
        className={`h-9 rounded-[6px] ${isInError ? textDisabled : ''}`}
      >
        M+
      </button>
      <button
        type="button"
        disabled={isInError}
        onClick={() => dispatch({ type: 'M_SUB' })}
        className={`h-9 rounded-[6px] ${isInError ? textDisabled : ''}`}
      >
        M−
      </button>
      <button
        type="button"
        disabled={isInError}
        onClick={() => dispatch({ type: 'MS' })}
        className={`h-9 rounded-[6px] ${isInError ? textDisabled : ''}`}
      >
        MS
      </button>
      <button type="button" disabled className={`h-9 rounded-[6px] ${textDisabled}`}>
        M∨
      </button>
    </div>
  );

  return (
    <div
      className={`min-h-screen w-full flex flex-col items-center justify-center p-4 gap-3 transition-colors ${
        dark ? 'bg-[#121212]' : 'bg-[#E5E7EB]'
      }`}
    >
      {/* Compact Preview Controls */}
      <div className="flex items-center gap-2 text-xs">
        <button
          type="button"
          onClick={() => setLang(lang === 'en' ? 'lt' : 'en')}
          className={`px-3 py-1.5 rounded-md border font-medium transition-colors ${
            dark
              ? 'bg-[#262626] border-[#3A3A3A] text-white'
              : 'bg-white border-neutral-300 text-neutral-800'
          }`}
        >
          {lang === 'en' ? 'EN (.)' : 'LT (,)'}
        </button>
        <button
          type="button"
          onClick={() => setDark(!dark)}
          className={`px-3 py-1.5 rounded-md border font-medium flex items-center gap-1.5 transition-colors ${
            dark
              ? 'bg-[#262626] border-[#3A3A3A] text-white'
              : 'bg-white border-neutral-300 text-neutral-800'
          }`}
        >
          {dark ? <Sun className="w-3.5 h-3.5" /> : <Moon className="w-3.5 h-3.5" />}
          {dark ? 'Dark' : 'Light'}
        </button>
        <button
          type="button"
          onClick={() => setLandscape(!landscape)}
          className={`px-3 py-1.5 rounded-md border font-medium flex items-center gap-1.5 transition-colors ${
            dark
              ? 'bg-[#262626] border-[#3A3A3A] text-white'
              : 'bg-white border-neutral-300 text-neutral-800'
          }`}
        >
          <RotateCw className="w-3.5 h-3.5" />
          {landscape ? 'Landscape' : 'Portrait'}
        </button>
      </div>

      {/* Device Frame */}
      <div
        className={`relative overflow-hidden rounded-2xl shadow-2xl border transition-all duration-200 ${bgMain} ${
          dark ? 'border-[#333333]' : 'border-black/10'
        } ${landscape ? 'w-[700px] max-w-full h-[380px]' : 'w-[360px] max-w-full h-[640px]'}`}
      >
        {landscape ? (
          <div className="flex h-full">
            <div className="w-[42%] flex flex-col justify-between">
              <div className="flex items-center justify-between px-4 pt-3 pb-1">
                <span className="text-[19px] font-semibold">{t.standard}</span>
                <button
                  type="button"
                  aria-label="History"
                  onClick={() => dispatch({ type: 'TOGGLE_HISTORY' })}
                  className="p-1.5 rounded-md hover:bg-black/5 dark:hover:bg-white/10"
                >
                  <History className="w-5 h-5" strokeWidth={1.7} />
                </button>
              </div>
              <div className="px-4 py-2 text-right">
                <div className={`text-[14px] min-h-[20px] truncate ${textSecondary}`}>
                  {state.expressionText || '\u00A0'}
                </div>
                <div className={`font-semibold tabular-nums leading-tight truncate ${displaySizeClass}`}>
                  {displayValue}
                </div>
              </div>
              {renderMemoryRow()}
            </div>
            <div className="w-[58%] h-full">{renderKeypad()}</div>
          </div>
        ) : (
          <div className="flex flex-col h-full">
            <div className="flex items-center justify-between px-4 pt-3 pb-1">
              <span className="text-[20px] font-semibold">{t.standard}</span>
              <button
                type="button"
                aria-label="History"
                onClick={() => dispatch({ type: 'TOGGLE_HISTORY' })}
                className="p-1.5 rounded-md hover:bg-black/5"
              >
                <History className="w-5 h-5" strokeWidth={1.7} />
              </button>
            </div>
            <div className="flex-[0.24] flex flex-col justify-end px-4 pb-2 text-right">
              <div className={`text-[15px] min-h-[22px] truncate ${textSecondary}`}>
                {state.expressionText || '\u00A0'}
              </div>
              <div className={`font-semibold tabular-nums leading-tight truncate ${displaySizeClass}`}>
                {displayValue}
              </div>
            </div>
            {renderMemoryRow()}
            <div className="flex-[0.76] min-h-0">{renderKeypad()}</div>
          </div>
        )}

        {/* History Overlay Sheet */}
        {state.isHistoryVisible && (
          <div className="absolute inset-0 z-20 flex flex-col justify-end">
            <div
              onClick={() => dispatch({ type: 'CLOSE_HISTORY' })}
              className={`absolute inset-0 ${dark ? 'bg-black/45' : 'bg-black/25'}`}
            />
            <div
              className={`relative z-10 flex flex-col rounded-t-xl shadow-xl ${
                landscape ? 'h-[85%]' : 'h-[66%]'
              } ${dark ? 'bg-[#2C2C2C] text-white' : 'bg-[#F9F9F9] text-[#1A1A1A]'}`}
            >
              <div className="flex-1 overflow-y-auto px-3 py-3 space-y-1">
                {state.history.length === 0 ? (
                  <p className={`px-3 pt-4 text-sm font-medium ${textSecondary}`}>{t.emptyHistory}</p>
                ) : (
                  state.history.map((entry) => (
                    <button
                      key={entry.id}
                      type="button"
                      onClick={() => dispatch({ type: 'RECALL_HISTORY', entry })}
                      className={`w-full text-right px-3 py-2.5 rounded-lg transition-colors ${
                        dark ? 'hover:bg-white/5' : 'hover:bg-black/5'
                      }`}
                    >
                      <div className={`text-sm ${textSecondary}`}>{entry.expression}</div>
                      <div className="text-[22px] font-semibold tabular-nums">{entry.resultFormatted}</div>
                    </button>
                  ))
                )}
              </div>
              <div className="flex justify-end px-3 py-2">
                <button
                  type="button"
                  aria-label="Clear history"
                  disabled={state.history.length === 0}
                  onClick={() => dispatch({ type: 'CLEAR_HISTORY' })}
                  className={`p-2 rounded-md transition-colors ${
                    state.history.length === 0 ? textDisabled : 'hover:bg-black/5'
                  }`}
                >
                  <Trash2 className="w-5 h-5" strokeWidth={1.7} />
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
