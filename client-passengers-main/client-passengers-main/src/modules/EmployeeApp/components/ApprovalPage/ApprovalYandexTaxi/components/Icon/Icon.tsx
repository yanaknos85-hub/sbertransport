import React, { FC } from 'react';

import WARNING_ICON from './assets/warningIcon.png';
import YANDEX_GO_ICON from './assets/yangexGo.png';
import PDF_ICON from './assets/pdfIcon.png';

import styles from './icon.module.scss';
import classNames from 'classnames';

interface ImageProps {
  src: string;
  hasRoundBorder?: boolean;
}

type IconTypes = 'pdf' | 'yandexGo' | 'warning';

const icons: Record<IconTypes, ImageProps> = {
  pdf: { src: PDF_ICON },
  yandexGo: { src: YANDEX_GO_ICON, hasRoundBorder: true },
  warning: { src: WARNING_ICON },
};

export interface IconProps {
  type: IconTypes;
  className?: string;
}

export const Icon: FC<IconProps> = ({ type, className }) => {
  if (type in icons) {
    const wrapperStyle = classNames(
      className,
      styles.imageWrapper,
      {
        [styles.withBorder]: icons[type].hasRoundBorder,
      }
    );
    return (
      <div className={wrapperStyle}>
        <img
          src={icons[type].src}
          className={styles.image}
          alt=""
        />
      </div>
    );
  }
  return null;
};
