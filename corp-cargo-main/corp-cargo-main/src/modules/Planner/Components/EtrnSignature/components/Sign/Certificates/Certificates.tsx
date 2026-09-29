import React, { FC } from 'react';
import { FileProtectOutlined } from '@ant-design/icons';
import moment from 'moment';
import { useTranslation } from 'i18n';
import cn from 'classnames';
import { DATE_FORMAT } from 'constants/constants.app';
import type { CertificateExtended } from 'modules/Planner/Components/EtrnSignature/cryptoPro.interface';

import styles from './styles.module.scss';

interface Props {
  selected: string | null;
  list: CertificateExtended[] | null;
  onSelect: (value: string) => void;
}

const Certificates: FC<Props> = ({ selected, list, onSelect }) => {
  const { signModal: i18 } = useTranslation().t.Etrn;

  return (
    <div className={styles.container}>
      {list?.map(({ name, active, thumbprint, validTo }) => (
        <div
          key={thumbprint}
          className={cn(styles.card, {
            [styles.card_selected]: selected === thumbprint,
            [styles.card_disabled]: !active,
          })}
          onClick={() => active ? onSelect(thumbprint) : null}
          role="button"
          aria-disabled={!active}
        >
          <FileProtectOutlined />

          <div className={styles.card__content}>
            <span>{name}</span>

            <div className={styles.block}>
              <span className={styles.block__label}>{i18.certificates.validTo}</span>
              <span>{moment(validTo).format(DATE_FORMAT.DATE_WITH_TIME_DOTS)}</span>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};

export default Certificates;
