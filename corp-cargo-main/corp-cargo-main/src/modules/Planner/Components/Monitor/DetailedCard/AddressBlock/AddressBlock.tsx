import React, { FC } from 'react';

import './styles.scss';

interface AddressProps {
  title?: string;
  from: string;
  to: string;
}

const AddressBlock: FC<AddressProps> = props => {
  const {
    title, from, to,
  } = props;

  return (
    <>
      <div className="w-100" title={title}>
        <div className="d-flex">
          <div className="circle circle-top-color circle-top">A</div>
          <div className="line-top" />
          <div className="address-block">
            <div className="address-label">Откуда</div>
            <div className="address-value">{from}</div>
          </div>
        </div>
      </div>
      <div className="w-100" title={title}>
        <div className="d-flex">
          <div className="circle circle-bottom-color circle-bottom">B</div>
          <div className="line-bottom" />
          <div className="address-block">
            <div className="address-label">Куда</div>
            <div className="address-value">{to}</div>
          </div>
        </div>
      </div>
    </>
  );
};

export default AddressBlock;
