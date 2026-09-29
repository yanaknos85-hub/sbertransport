import React, { FC } from 'react';
import cn from 'classnames';
import { ReactComponent as ControlSVG } from 'assets/icons/control.svg';
import { ReactComponent as ImportSVG } from 'assets/icons/import.svg';
import { ReactComponent as ExportSVG } from 'assets/icons/export.svg';
import { ReactComponent as CloseModalCVG } from 'assets/icons/close.svg';
import { ReactComponent as HomeSVG } from 'assets/icons/home.svg';
import { ReactComponent as CaseSVG } from 'assets/icons/case.svg';
import { ReactComponent as ChatSVG } from 'assets/icons/group-chat.svg';
import { ReactComponent as MailSVG } from 'assets/icons/mail.svg';
import { ReactComponent as PdfSVG } from 'assets/icons/pdf-icon.svg';
import { ReactComponent as SettingsSVG } from 'assets/icons/settings.svg';
import { ReactComponent as DragSVG } from 'assets/icons/drag.svg';

import styles from './Icon.module.scss';

const icons = {
  control: <ControlSVG />,
  import: <ImportSVG />,
  export: <ExportSVG />,
  closeModal: <CloseModalCVG />,
  home: <HomeSVG />,
  case: <CaseSVG />,
  chat: <ChatSVG />,
  mail: <MailSVG />,
  pdf: <PdfSVG />,
  settings: <SettingsSVG />,
  drag: <DragSVG />,
};

export type IconType = keyof typeof icons;

interface Props {
  type: keyof typeof icons;
  color?: string;
  transform?: 'rotate90' | 'rotate180';
  className?: string;
}

export const Icon: FC<Props> = ({
  color, transform, type, className,
}) => (
  <span
    style={{ color }}
    className={cn(styles.icon, className, {
      [styles.icon_rotate90]: transform === 'rotate90',
      [styles.icon_rotate180]: transform === 'rotate180',
    })}
  >
    {icons[type]}
  </span>
);
