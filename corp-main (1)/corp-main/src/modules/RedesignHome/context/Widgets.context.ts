import { useState, useCallback } from 'react';

import { createCallableCtx } from 'utils/createCallableContext';

import { Widgets } from '../types/Home.types';

const DEFAULT_WIDGET_RUN_STATE = Object.keys(Widgets).reduce((a, name) => ({ ...a, [name]: false }), {}) as Record<
  Widgets,
  boolean
>;

const useHook = () => {
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [activeWidgets, setActiveWidgets] = useState<Set<Widgets>>(new Set(Object.keys(Widgets) as Widgets[]));
  const [formActiveWidgets, setFormActiveWidgets] = useState<Set<Widgets>>(new Set());

  const [widgetIsRun, setWidgetIsRun] = useState(DEFAULT_WIDGET_RUN_STATE);
  const [widgetIsReload, setWidgetIsReload] = useState(DEFAULT_WIDGET_RUN_STATE);

  const openModal = useCallback(() => {
    setIsModalVisible(true);
    setFormActiveWidgets(activeWidgets);
  }, [activeWidgets]);

  const closeModal = useCallback(() => setIsModalVisible(false), []);

  const toggleFormWidget = useCallback((widget: Widgets) => {
    setFormActiveWidgets(prevActiveWidgets => {
      const newActiveWidgets = new Set(prevActiveWidgets);

      if (newActiveWidgets.has(widget)) {
        newActiveWidgets.delete(widget);
      } else {
        newActiveWidgets.add(widget);
      }

      return newActiveWidgets;
    });
  }, []);

  const saveNewWidgets = useCallback(() => {
    setActiveWidgets(formActiveWidgets);
    closeModal();
  }, [formActiveWidgets, closeModal]);

  return {
    isModalVisible,
    openModal,
    closeModal,
    activeWidgets,
    formActiveWidgets,
    toggleFormWidget,
    saveNewWidgets,
    widgetIsRun,
    setWidgetIsRun: (widgetName: Widgets) => setWidgetIsRun({ ...widgetIsRun, [widgetName]: true }),
    widgetIsReload,
    reloadWidget: (widgetName: Widgets) => {
      return setWidgetIsReload({ ...widgetIsReload, [widgetName]: !widgetIsReload[widgetName] });
    },
  };
};

export const [useWidgets, WidgetsProvider] = createCallableCtx(useHook, { name: 'WidgetsProvider' });
