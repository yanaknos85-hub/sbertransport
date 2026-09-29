import React, { FC } from 'react';

import styles from './style.module.scss';

interface MarkerWithLetterProps {
  src: string;
  letter: string;
  alt: string;
  style?: React.CSSProperties;
}

export const MarkerWithLetter: FC<MarkerWithLetterProps> = ({
  src, letter, alt, style,
}) => {
  return (
    <div className={styles.markerContainer} style={style}>
      <img
        src={src}
        style={{
          position: 'relative',
          width: '100%',
          height: '100%',
          pointerEvents: 'none',
        }}
        width={24}
        height={24}
        alt={alt}
      />
      <span className={styles.markerLetter}>
        {letter}
      </span>
    </div>
  );
};
