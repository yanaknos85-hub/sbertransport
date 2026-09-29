import React, { FC } from 'react';
import cn from 'classnames';
import { useUiContext } from 'shared/components/UI';
import TButton from 'shared/ui/Button/Button';

import { FilterById } from '../../FilterById/FilterById';
import { SortMenu } from '../../SortMenu/SortMenu';

import styles from './Controls.module.scss';

interface Props {
  onShow: () => void;
  descriptionAddressFrom: string;
  descriptionAddressTo: string;
}

export const Controls: FC<Props> = props => {
  const { isMobile } = useUiContext();

  const {
    onShow, descriptionAddressFrom, descriptionAddressTo,
  } = props;

  return (
    <div className={cn(styles.controls, {
      [styles['isMobileDirection']]: isMobile,
    })}
    >
      <div className={cn(styles.filtersWrapper, {
        [styles['isMobileDirection']]: isMobile,
        [styles['filtersWrapperMobile']]: isMobile,
      })}
      >
        <FilterById isCrossVisible />
        <TButton
          $size="small"
          className={styles.filterButton}
          onClick={() => onShow()}
        >
          Фильтры
        </TButton>
        {descriptionAddressFrom && (
          <div>
            Адрес отправления:
            {' '}
            <p style={{ fontWeight: 600 }}>{descriptionAddressFrom}</p>
          </div>
        )}
        {descriptionAddressTo && (
          <div>
            Адрес доставки:
            {' '}
            <p style={{ fontWeight: 600 }}>{descriptionAddressTo}</p>
          </div>
        )}
      </div>
      <SortMenu />
    </div>
  );
};
