import React, {
  Dispatch, FC, MutableRefObject, ReactNode
} from 'react';
import { Input } from 'antd';
import { useTranslation } from 'i18n';
import { FiltersRegistryWrapper } from '../Filters/Filters';
import styles from './ActionsRegistry.module.scss';

interface Props {
  idValue?: string;
  handleSearchId?: (id: string) => void;
  buttons?: ReactNode[];
  filters?: ReactNode;
  isVisible?: boolean;
  setIsVisible?: Dispatch<React.SetStateAction<boolean>>;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  buttonsRef?: MutableRefObject<any>;
  isNewDesign?: boolean;
  handleChangeId?: (id: string) => void;
}

export const ActionsRegistry: FC<Props> = ({
  idValue,
  buttons,
  filters,
  handleSearchId,
  isVisible,
  setIsVisible,
  buttonsRef,
  isNewDesign,
  handleChangeId,
}) => {
  const { t } = useTranslation();

  return (
    <div className={styles.container}>
      {buttonsRef && <div ref={buttonsRef} />}

      <div className={styles.controls}>
        <div className={styles.searchContainer}>
          <Input.Search
            className={styles.search}
            placeholder={t.Forms.registryFilterFields.searchByTicketId}
            onSearch={handleSearchId}
            value={idValue}
            disabled={!handleSearchId}
            onChange={e => handleChangeId && handleChangeId(e.target.value)}
          />
          <FiltersRegistryWrapper
            isNewDesign={isNewDesign}
            isVisible={isVisible}
            setIsVisible={setIsVisible}
          >
            {filters}
          </FiltersRegistryWrapper>
        </div>
        <div className={styles.buttons} style={{ justifyContent: 'end' }}>
          {buttons}
        </div>
      </div>
    </div>
  );
};
