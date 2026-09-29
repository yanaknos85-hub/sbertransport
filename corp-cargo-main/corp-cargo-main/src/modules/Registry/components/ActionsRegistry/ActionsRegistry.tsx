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
  handleChangeId?: (id: string) => void;
  buttons?: ReactNode[];
  filters?: ReactNode;
  isVisible?: boolean;
  setIsVisible?: Dispatch<React.SetStateAction<boolean>>;
  buttonsRef?: MutableRefObject<any>;
  isNewDesign?: boolean;
  placeholder?: string;
  showSearch?: boolean;
}

export const ActionsRegistry: FC<Props> = ({
  idValue,
  buttons,
  filters,
  handleSearchId,
  handleChangeId,
  isVisible,
  setIsVisible,
  buttonsRef,
  isNewDesign,
  placeholder,
  showSearch = true
}) => {
  const { t } = useTranslation();

  return (
    <div className={styles.container}>
      {buttonsRef && <div ref={buttonsRef} />}

      <div className={styles.controls}>
        <div className={styles.searchContainer}>
          {showSearch && (
            <Input.Search
              className={styles.search}
              placeholder={placeholder || t.Forms.registryFilterFields.searchByTicketId}
              onSearch={handleSearchId}
              onChange={e => handleChangeId && handleChangeId(e.target.value)}
              value={idValue}
              disabled={!handleSearchId}
            />
          )}
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
