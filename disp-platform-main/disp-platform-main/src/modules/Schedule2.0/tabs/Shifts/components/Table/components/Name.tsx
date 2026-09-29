import React, {
  FC, MutableRefObject, useEffect, useRef, useState
} from 'react';
import styles from '../Table.module.scss';

interface NameProps {
  name: string;
  table: MutableRefObject<HTMLDivElement | null>;
  fixedColumnsWidth: number;
  hasEwbId?: boolean;
}

export const Name: FC<NameProps> = ({
  name, table, fixedColumnsWidth, hasEwbId = false,
}) => {
  const ref = useRef<HTMLSpanElement>(null);
  const [left, setLeft] = useState(0);

  useEffect(() => {
    const tableRef = table.current;

    const fixName = () => {
      const tableLeftOffset = tableRef?.getBoundingClientRect().x ?? 0;
      const parentRect = ref.current?.parentElement?.getBoundingClientRect() ?? { left: 0, width: 0 };
      const nameX = parentRect.left - tableLeftOffset - fixedColumnsWidth;
      const fixDelta = 200;

      if (nameX < fixDelta && Math.abs(nameX) <= parentRect.width)
        setLeft(nameX < 0 ? -nameX : 0);
    };

    tableRef?.addEventListener('scroll', fixName);

    return () => tableRef?.removeEventListener('scroll', fixName);
  }, [table, ref, fixedColumnsWidth]);

  return (
    <span
      className={styles.stateNumber}
      ref={ref}
      style={{ transform: `translate(${left}px)` }}
    >
      {name}

      {hasEwbId && (
        <span className={styles.eplBadge}>
          ЭПЛ
        </span>
      )}
    </span>
  );
};
