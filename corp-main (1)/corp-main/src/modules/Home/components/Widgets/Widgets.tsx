import React, { FC, Fragment, Suspense } from 'react';
import { useTranslation } from 'i18n';

import ErrorBoundary from 'shared/components/ErrorBoundary';
import SpinWrapped from 'shared/components/SpinWrapped/SpinWrapped';

import { Container } from '../Container/Container';
import { ModalCheckbox } from '../ModalCheckbox/ModalCheckbox';
import { useWidgets } from '../../context/Widgets.context';
import { widgetComponents, widgets } from '../../constants/widgets';

import styles from './Widgets.module.scss';
import { ContainerWidget } from '../ContainerWidget/ContainerWidget';

export const Widgets: FC = () => {
  const { t } = useTranslation();
  const {
    activeWidgets,
    openModal,
    isModalVisible,
    closeModal,
    saveNewWidgets,
    formActiveWidgets,
    toggleFormWidget,
  } = useWidgets();

  return (
    <Fragment key="Widgets">
      {activeWidgets.size > 0 && (
        <Container title={t.HomePage.widgets} margin="16px 0 0 0">
          {Array.from(activeWidgets).map(widget => {
            const Widget = widgetComponents[widget];

            return (
              <ErrorBoundary key={widget}>
                <Suspense fallback={<SpinWrapped />}>
                  <ContainerWidget>
                    <Widget />
                  </ContainerWidget>
                </Suspense>
              </ErrorBoundary>
            );
          })}
        </Container>
      )}

      <Container margin="16px 0 0 0">
        <button
          className={styles.addButton}
          type="button"
          onClick={openModal}
        >
          +
          {' '}
          {t.Widgets.newButton}
        </button>
      </Container>

      <ModalCheckbox
        title={t.Widgets.Modal.title}
        description={t.Widgets.Modal.description}
        isModalVisible={isModalVisible}
        closeModal={closeModal}
        saveNew={saveNewWidgets}
        formActive={formActiveWidgets}
        toggleForm={toggleFormWidget}
        checkboxes={widgets}
        translationModuleKey="Widgets"
      />
    </Fragment>
  );
};
