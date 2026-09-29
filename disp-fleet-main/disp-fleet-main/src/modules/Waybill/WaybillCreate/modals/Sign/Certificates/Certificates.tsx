import React from 'react';
import type { FC } from 'react';
import { FileProtectOutlined } from '@ant-design/icons';
import moment from 'moment';
import { useTranslation } from 'i18n';
import cn from 'classnames';

import styles from './Certificates.module.scss';
import { CertificateExtended } from 'types/cryptoPro.interface';
import { DATE_FORMAT } from 'constants/app.constants';

interface Props {
  selected: string | null;
  list: CertificateExtended[] | null;
  onSelect: (value: string) => void;
}

const Certificates: FC<Props> = ({
  selected,
  list,
  onSelect,
}) => {
  const { sign: i18 } = useTranslation().t.Waybill.modal;

  return (
    <div className={styles.container}>
      {list?.map(({
        name, active, thumbprint, validTo,
      }) => (
        <div
          className={cn(styles.card, {
            [styles.card_selected]: selected === thumbprint,
            [styles.card_disabled]: !active,
          })}
          onClick={() => active ? onSelect(thumbprint) : null}
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
