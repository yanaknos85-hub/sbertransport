import React, { MouseEvent } from 'react';
import { Button } from 'antd';
import { observer } from 'mobx-react';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { AddressModel } from 'stores/Address/models/Address.model';
import { StoreNames } from 'stores/StoreNames.enum';

import styles from './styles.module.scss';

export const FrequentAddressPicker = observer(({ onSelect }: { onSelect: (item: AddressModel) => void }) => {
  const { [StoreNames.addressStore]: address } = useAppStoreContext();

  const clickItemHandler = (e: MouseEvent<any>, item: AddressModel): void => {
    e.stopPropagation();
    onSelect(item);
  };

  return (
    // eslint-disable-next-line react/jsx-no-useless-fragment
    <>
      {/* FIXME react/jsx-no-useless-fragment */}
      {address.selfFrequentList.slice(0, 4).map(x => (
        <Button
          key={x.id}
          shape="round"
          className={styles.button}
          size="small"
          onClick={(e): void => clickItemHandler(e, x)}
        >
          {x.addressString}
        </Button>
      ))}
    </>
  );
});
