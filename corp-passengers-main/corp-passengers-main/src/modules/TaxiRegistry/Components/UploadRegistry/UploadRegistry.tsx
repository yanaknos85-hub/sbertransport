import React from 'react';
import ContractorRegistries from './ContractorRegistries/ContractorRegistries';
import { UploadContractorRegistry } from './UploadContractorRegistry/UploadContractorRegistry';
import styles from './asset/styles.module.scss';

export const UploadRegistry = (): JSX.Element => (
  <div className={styles.subMenu}>
    <ContractorRegistries />
    <UploadContractorRegistry />
  </div>
);
