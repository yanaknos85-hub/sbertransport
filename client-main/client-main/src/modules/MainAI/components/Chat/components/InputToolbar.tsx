import React, { FC, useCallback, useRef } from 'react';
import cn from 'classnames';
import { ReactComponent as SendIcon } from 'shared/icons/send.svg';
import { ReactComponent as MicroIcon } from 'shared/icons/micro.svg';
import { ReactComponent as ClearIcon } from 'shared/icons/clear.svg';
import { ReactComponent as StopIcon } from 'shared/icons/stop.svg';
import { ReactComponent as StopRedIcon } from 'shared/icons/stop-red.svg';
import { formatDuration } from 'utils/formatDuration';

import styles from './InputToolbar.module.scss';

interface InputToolbarProps {
  /** Значение в текстовом поле */
  inputValue: string;
  /** Идёт ли запись */
  isRecording: boolean;
  /** Идёт ли обработка аудио */
  isProcessingAudio: boolean;
  /** Длительность записи (сек) */
  audioDuration: number;
  /** Массив уровней звука 0..1 для каждой полоски */
  audioLevels: number[];
  /** Идёт ли загрузка сообщения */
  isLoading: boolean;
  /** Изменить значение */
  onChange: (value: string) => void;
  /** Отправить сообщение */
  onSend: () => void;
  /** Начать запись */
  onStartRecording: () => void;
  /** Остановить запись */
  onStopRecording: () => void;
}

/**
 * Индикатор уровня звука в виде набора полосок.
 * Каждая полоска меняет высоту пропорционально audioLevels.
 * При тишине — все полоски ровные (минимальная высота).
 */
const AudioVisualizer: FC<{ levels: number[] }> = ({ levels }) => (
  <div className={styles.audioVisualizer}>
    {levels.map((level, i) => (
      <span
        key={i}
        className={styles.audioBar}
        style={{
          height: `${Math.max(4, 2 + level * 14)}px`,
          opacity: Math.max(0.3, 0.3 + level * 0.7),
        }}
      />
    ))}
  </div>
);

/**
 * Таблица состояний для блока действий (кнопки справа).
 *
 * | isRecording | isProcessingAudio | inputValue | Кнопка |
 * |-------------|------------------|------------|--------|
 * | да          | нет              | —          | Stop   |
 * | нет         | да               | —          | Processing (disabled) |
 * | нет         | нет              | непусто    | Send   |
 * | нет         | нет              | пусто      | Micro  |
 */
interface ActionButtonsProps {
  canInput: boolean;
  isLoading: boolean;
  inputValue: string;
  isRecording: boolean;
  isProcessingAudio: boolean;
  onStopRecording: () => void;
  onSend: () => void;
  onStartRecording: () => void;
}

const ActionButtons: FC<ActionButtonsProps> = ({
  canInput,
  isLoading,
  inputValue,
  isRecording,
  isProcessingAudio,
  onStopRecording,
  onSend,
  onStartRecording,
}) => (
  <div className={styles.actions}>
    {isRecording && (
      <button
        className={cn(styles.actionBtn, styles.stopBtn)}
        onClick={onStopRecording}
        type="button"
        aria-label="Остановить запись"
      >
        <StopIcon />
      </button>
    )}
    {isProcessingAudio && (
      <button
        className={cn(styles.actionBtn, styles.processingBtn)}
        disabled
        type="button"
        aria-label="Идет обработка"
      >
        <StopRedIcon />
      </button>
    )}
    {canInput && inputValue && (
      <button
        className={cn(styles.actionBtn, styles.sendBtn)}
        onClick={onSend}
        disabled={isLoading || !inputValue.trim()}
      >
        <SendIcon />
      </button>
    )}
    {canInput && !inputValue && (
      <button
        className={cn(styles.actionBtn, styles.microBtn)}
        onClick={onStartRecording}
        type="button"
        aria-label="Голосовой ввод"
        disabled={isLoading}
      >
        <MicroIcon />
      </button>
    )}
  </div>
);

const InputToolbar: FC<InputToolbarProps> = ({
  inputValue,
  isRecording,
  isProcessingAudio,
  audioDuration,
  audioLevels,
  isLoading,
  onChange,
  onSend,
  onStartRecording,
  onStopRecording,
}) => {
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const autoResize = useCallback(() => {
    const el = textareaRef.current;
    if (!el) return;
    el.style.height = 'auto';
    el.style.height = `${el.scrollHeight}px`;
  }, []);

  const resetHeight = useCallback(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = '';
      textareaRef.current.rows = 1;
    }
  }, []);

  const handleClear = useCallback(() => {
    onChange('');
    resetHeight();
  }, [onChange, resetHeight]);

  const canInput = !isRecording && !isProcessingAudio;

  return (
    <div className={styles.inputPanel}>
      <textarea
        ref={textareaRef}
        className={cn(styles.input, { [styles.inputDisabled]: !canInput })}
        placeholder={
          isProcessingAudio
            ? 'идет обработка записи...'
            : isRecording
              ? 'идет запись голоса, по окончанию нажмите стоп'
              : 'Спросите у помощника'
        }
        value={inputValue}
        onChange={e => {
          if (!canInput) return;
          onChange(e.target.value);
          // TODO: Использование setTimeout для вызова autoResize в обработчике onChange может привести к накоплению таймаутов при быстром вводе, так как отмена предыдущих не предусмотрена.
          // Заменить setTimeout на requestAnimationFrame для синхронизации с рендером и предотвращения накопления.
          setTimeout(autoResize);
        }}
        onKeyDown={e => {
          if (!canInput) return;
          if (e.key === 'Enter') onSend();
        }}
        rows={1}
        disabled={!canInput}
        // eslint-disable-next-line jsx-a11y/no-autofocus
        autoFocus
      />

      {canInput && inputValue && (
        <button
          className={styles.clearBtn}
          onClick={handleClear}
          type="button"
          aria-label="Очистить"
        >
          <ClearIcon />
        </button>
      )}

      {isRecording && (
        <div className={styles.recordingBadge}>
          <AudioVisualizer levels={audioLevels} />
          <span className={styles.recordingTimer}>
            {formatDuration(audioDuration)}
          </span>
        </div>
      )}

      <ActionButtons
        canInput={canInput}
        isLoading={isLoading}
        inputValue={inputValue}
        isRecording={isRecording}
        isProcessingAudio={isProcessingAudio}
        onStopRecording={onStopRecording}
        onSend={onSend}
        onStartRecording={onStartRecording}
      />
    </div>
  );
};

export default InputToolbar;
