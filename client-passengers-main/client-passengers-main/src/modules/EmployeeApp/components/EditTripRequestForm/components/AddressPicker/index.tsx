
import classNames from 'classnames';
import React, { FC } from 'react';

import { AddressModel } from 'stores/Address/models/Address.model';

import { FavoriteAddressPicker } from './FavoriteAddressPicker';
import { FrequentAddressPicker } from './FrequentAddressPicker';

import styles from './styles.module.scss';

const AddressPicker: FC<{
  className?: string;
  onSelect: (item: AddressModel) => void;
}> = ({ className, onSelect }) => (
  <div className={classNames(styles.wrapper, className)}>
    <FavoriteAddressPicker onSelect={onSelect} />
    <FrequentAddressPicker onSelect={onSelect} />
  </div>
);

export default AddressPicker;
