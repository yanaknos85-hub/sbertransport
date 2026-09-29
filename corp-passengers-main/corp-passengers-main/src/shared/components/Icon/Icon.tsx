import React, { FC } from 'react';
import cn from 'classnames';
import { ReactComponent as ControlSVG } from '../../icons/control.svg';
import { ReactComponent as ImportSVG } from '../../icons/import.svg';
import { ReactComponent as ExportSVG } from '../../icons/export.svg';
import { ReactComponent as ArrowLeftSVG } from '../../icons/arrow-left.svg';
import { ReactComponent as HomeSVG } from '../../assets/svg/Home.svg';
import { ReactComponent as CaseSVG } from '../../assets/svg/Case.svg';
import { ReactComponent as CloseModalCVG } from '../../assets/svg/Close.svg';
import { ReactComponent as SettingsGear } from '../../assets/svg/SettingsGear.svg';
import { ReactComponent as ChatSVG } from '../../assets/svg/chat.svg';
import { ReactComponent as PhoneSVG } from '../../assets/svg/phone.svg';
import { ReactComponent as MailSVG } from '../../assets/svg/mail.svg';
import { ReactComponent as PlusSVG } from '../../assets/svg/Plus.svg';

import styles from './Icon.module.scss';

const icons = {
  control: <ControlSVG />,
  import: <ImportSVG />,
  export: <ExportSVG />,
  arrowLeft: <ArrowLeftSVG />,
  home: <HomeSVG />,
  case: <CaseSVG />,
  closeModal: <CloseModalCVG />,
  settingsGear: <SettingsGear />,
  chat: <ChatSVG />,
  phone: <PhoneSVG />,
  mail: <MailSVG />,
  plus: <PlusSVG />,
};

export type IconType = keyof typeof icons;

interface Props {
  type: IconType;
  color?: string;
  transform?: 'rotate90' | 'rotate180';
  className?: string;
}

export const Icon: FC<Props> = ({
  color, transform, type, className,
}) => (
  <span
    style={{ color }}
    className={cn(
      styles.icon,
      {
        [styles.icon_rotate90]: transform === 'rotate90',
        [styles.icon_rotate180]: transform === 'rotate180',
      },
      [className]
    )}
  >
    {icons[type]}
  </span>
);
