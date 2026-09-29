import React from 'react';
import { render } from '@testing-library/react';

import Stepper from '../Stepper/Stepper';

jest.mock('../Stepper/Stepper.style', () => {
  const mockComponent =
    (name: string) =>
    ({ children, ...rest }: { children?: React.ReactNode }) => (
      <div data-component={name} {...rest}>
        {children}
      </div>
    );

  return {
    StepperDiv: mockComponent('StepperDiv'),
    FirstStepDiv: mockComponent('FirstStepDiv'),
    MiddleStepDiv: mockComponent('MiddleStepDiv'),
    LastStepDiv: mockComponent('LastStepDiv'),
    EmptyIcon: mockComponent('EmptyIcon'),
    CurrentIcon: mockComponent('CurrentIcon'),
    CurrentInnerIcon: mockComponent('CurrentInnerIcon'),
    DoneIcon: mockComponent('DoneIcon'),
    EmptyEdge: mockComponent('EmptyEdge'),
    CurrentEdge: mockComponent('CurrentEdge'),
    DoneEdge: mockComponent('DoneEdge'),
  };
});

describe('Stepper', () => {
  describe('структура DOM', () => {
    it('рендерит корневой контейнер StepperDiv', () => {
      const { container } = render(<Stepper steps={3} active={0} />);

      const root = container.querySelector('[data-component="StepperDiv"]');
      expect(root).not.toBeNull();
    });

    it('рендерит ровно steps элементов', () => {
      const { container } = render(<Stepper steps={7} active={3} />);

      const stepper = container.querySelector('[data-component="StepperDiv"]')!;
      const steps = stepper.querySelectorAll(
        '[data-component="FirstStepDiv"], [data-component="MiddleStepDiv"], [data-component="LastStepDiv"]',
      );
      expect(steps).toHaveLength(7);
    });

    it('не рендерит MiddleStep когда steps=2', () => {
      const { container } = render(<Stepper steps={2} active={1} />);

      const stepper = container.querySelector('[data-component="StepperDiv"]')!;
      expect(stepper.querySelector('[data-component="FirstStepDiv"]')).not.toBeNull();
      expect(stepper.querySelector('[data-component="LastStepDiv"]')).not.toBeNull();
      expect(stepper.querySelector('[data-component="MiddleStepDiv"]')).toBeNull();
    });

    it('рендерит только first и last для steps=1', () => {
      const { container } = render(<Stepper steps={1} active={0} />);

      const stepper = container.querySelector('[data-component="StepperDiv"]')!;
      const steps = stepper.querySelectorAll(
        '[data-component="FirstStepDiv"], [data-component="MiddleStepDiv"], [data-component="LastStepDiv"]',
      );
      expect(steps).toHaveLength(2);
    });

    it('первый элемент — FirstStep, последний — LastStep, между ними — MiddleStep', () => {
      const { container } = render(<Stepper steps={5} active={2} />);

      const stepper = container.querySelector('[data-component="StepperDiv"]')!;
      const children = Array.from(stepper.children) as HTMLElement[];
      expect(children[0].getAttribute('data-component')).toBe('FirstStepDiv');
      expect(children[children.length - 1].getAttribute('data-component')).toBe('LastStepDiv');
      children.slice(1, -1).forEach(child => {
        expect(child.getAttribute('data-component')).toBe('MiddleStepDiv');
      });
    });
  });

  describe('статус FirstStep', () => {
    it('рендерит CurrentIcon для active=0', () => {
      const { container } = render(<Stepper steps={3} active={0} />);

      const first = container.querySelector('[data-component="FirstStepDiv"]')!;
      expect(first.querySelector('[data-component="CurrentIcon"]')).not.toBeNull();
      expect(first.querySelector('[data-component="DoneIcon"]')).toBeNull();
      expect(first.querySelector('[data-component="EmptyIcon"]')).toBeNull();
    });

    it('рендерит DoneIcon для active>0', () => {
      const { container } = render(<Stepper steps={3} active={1} />);

      const first = container.querySelector('[data-component="FirstStepDiv"]')!;
      expect(first.querySelector('[data-component="DoneIcon"]')).not.toBeNull();
      expect(first.querySelector('[data-component="CurrentIcon"]')).toBeNull();
    });
  });

  describe('статус MiddleStep для steps=7', () => {
    it('все middle пустые при active=0', () => {
      const { container } = render(<Stepper steps={7} active={0} />);

      const middles = container.querySelectorAll('[data-component="MiddleStepDiv"]');
      middles.forEach(middle => {
        expect(middle.querySelector('[data-component="EmptyIcon"]')).not.toBeNull();
        expect(middle.querySelector('[data-component="CurrentIcon"]')).toBeNull();
        expect(middle.querySelector('[data-component="DoneIcon"]')).toBeNull();
      });
    });

    it('для active=3 — middle с i<3 done, i=3 current, i>3 empty', () => {
      const { container } = render(<Stepper steps={7} active={3} />);

      const middles = container.querySelectorAll('[data-component="MiddleStepDiv"]');
      // middles[0] -> i=1 (done)
      // middles[1] -> i=2 (done)
      // middles[2] -> i=3 (current)
      // middles[3] -> i=4 (empty)
      // middles[4] -> i=5 (empty)

      expect(middles[0].querySelector('[data-component="DoneIcon"]')).not.toBeNull();
      expect(middles[1].querySelector('[data-component="DoneIcon"]')).not.toBeNull();
      expect(middles[2].querySelector('[data-component="CurrentIcon"]')).not.toBeNull();
      expect(middles[3].querySelector('[data-component="EmptyIcon"]')).not.toBeNull();
      expect(middles[4].querySelector('[data-component="EmptyIcon"]')).not.toBeNull();
    });

    it('все middle завершены при active=6', () => {
      const { container } = render(<Stepper steps={7} active={6} />);

      const middles = container.querySelectorAll('[data-component="MiddleStepDiv"]');
      expect(middles).toHaveLength(5);
      middles.forEach(middle => {
        expect(middle.querySelector('[data-component="DoneIcon"]')).not.toBeNull();
        expect(middle.querySelector('[data-component="CurrentIcon"]')).toBeNull();
        expect(middle.querySelector('[data-component="EmptyIcon"]')).toBeNull();
      });
    });
  });

  describe('статус LastStep', () => {
    it('рендерит EmptyIcon когда active < steps-1', () => {
      const { container } = render(<Stepper steps={7} active={5} />);

      const last = container.querySelector('[data-component="LastStepDiv"]')!;
      expect(last.querySelector('[data-component="EmptyIcon"]')).not.toBeNull();
    });

    it('рендерит DoneIcon когда active === steps-1 (все шаги завершены)', () => {
      const { container } = render(<Stepper steps={7} active={6} />);

      const last = container.querySelector('[data-component="LastStepDiv"]')!;
      expect(last.querySelector('[data-component="DoneIcon"]')).not.toBeNull();
      expect(last.querySelector('[data-component="CurrentIcon"]')).toBeNull();
    });

    it('рендерит CurrentIcon когда active > steps-1 (выход за диапазон)', () => {
      const { container } = render(<Stepper steps={7} active={7} />);

      const last = container.querySelector('[data-component="LastStepDiv"]')!;
      expect(last.querySelector('[data-component="CurrentIcon"]')).not.toBeNull();
      expect(last.querySelector('[data-component="DoneIcon"]')).toBeNull();
    });

    it('рендерит DoneIcon для steps=2 active=1', () => {
      const { container } = render(<Stepper steps={2} active={1} />);

      const last = container.querySelector('[data-component="LastStepDiv"]')!;
      expect(last.querySelector('[data-component="DoneIcon"]')).not.toBeNull();
    });
  });

  describe('все 7 шагов завершены (важный сценарий)', () => {
    it('все 7 иконок — DoneIcon при steps=7 active=6', () => {
      const { container } = render(<Stepper steps={7} active={6} />);

      const stepper = container.querySelector('[data-component="StepperDiv"]')!;
      expect(stepper.querySelectorAll('[data-component="DoneIcon"]')).toHaveLength(7);
      expect(stepper.querySelector('[data-component="CurrentIcon"]')).toBeNull();
      expect(stepper.querySelector('[data-component="EmptyIcon"]')).toBeNull();
    });

    it('все 3 иконки — DoneIcon при steps=3 active=2', () => {
      const { container } = render(<Stepper steps={3} active={2} />);

      const stepper = container.querySelector('[data-component="StepperDiv"]')!;
      expect(stepper.querySelectorAll('[data-component="DoneIcon"]')).toHaveLength(3);
    });

    it('оба элемента — DoneIcon при steps=2 active=1', () => {
      const { container } = render(<Stepper steps={2} active={1} />);

      const stepper = container.querySelector('[data-component="StepperDiv"]')!;
      expect(stepper.querySelectorAll('[data-component="DoneIcon"]')).toHaveLength(2);
    });
  });

  describe('содержимое иконок', () => {
    it('DoneIcon содержит CheckOutlined (галочку)', () => {
      const { container } = render(<Stepper steps={7} active={6} />);

      const doneIcon = container.querySelector('[data-component="DoneIcon"]')!;
      expect(doneIcon.querySelector('.anticon-check')).not.toBeNull();
    });

    it('CurrentIcon содержит CurrentInnerIcon', () => {
      const { container } = render(<Stepper steps={7} active={0} />);

      const currentIcon = container.querySelector('[data-component="CurrentIcon"]')!;
      expect(currentIcon.querySelector('[data-component="CurrentInnerIcon"]')).not.toBeNull();
    });

    it('DoneIcon не содержит CurrentInnerIcon', () => {
      const { container } = render(<Stepper steps={7} active={6} />);

      const doneIcon = container.querySelector('[data-component="DoneIcon"]')!;
      expect(doneIcon.querySelector('[data-component="CurrentInnerIcon"]')).toBeNull();
    });

    it('EmptyIcon не содержит ни галочки, ни внутреннего круга', () => {
      const { container } = render(<Stepper steps={7} active={0} />);

      const emptyIcon = container.querySelector('[data-component="EmptyIcon"]')!;
      expect(emptyIcon.querySelector('.anticon-check')).toBeNull();
      expect(emptyIcon.querySelector('[data-component="CurrentInnerIcon"]')).toBeNull();
    });
  });

  describe('связь иконки и edge внутри шага', () => {
    it('FirstStep рендерит один edge того же статуса, что и иконка (active=0 → current)', () => {
      const { container } = render(<Stepper steps={3} active={0} />);

      const first = container.querySelector('[data-component="FirstStepDiv"]')!;
      expect(first.querySelector('[data-component="CurrentEdge"]')).not.toBeNull();
      expect(first.querySelector('[data-component="EmptyEdge"]')).toBeNull();
      expect(first.querySelector('[data-component="DoneEdge"]')).toBeNull();
    });

    it('FirstStep рендерит DoneEdge когда active>0', () => {
      const { container } = render(<Stepper steps={3} active={2} />);

      const first = container.querySelector('[data-component="FirstStepDiv"]')!;
      expect(first.querySelector('[data-component="DoneEdge"]')).not.toBeNull();
    });

    it('MiddleStep рендерит по два edge того же статуса (i=1 active=3 → current)', () => {
      const { container } = render(<Stepper steps={7} active={3} />);

      const middles = container.querySelectorAll('[data-component="MiddleStepDiv"]');
      // middles[1] -> i=2, done
      // middles[2] -> i=3, current
      expect(middles[1].querySelectorAll('[data-component="DoneEdge"]')).toHaveLength(2);
      expect(middles[2].querySelectorAll('[data-component="CurrentEdge"]')).toHaveLength(2);
      expect(middles[2].querySelectorAll('[data-component="EmptyEdge"]')).toHaveLength(0);
    });

    it('LastStep рендерит edge своего статуса (active=6 → done)', () => {
      const { container } = render(<Stepper steps={7} active={6} />);

      const last = container.querySelector('[data-component="LastStepDiv"]')!;
      expect(last.querySelector('[data-component="DoneEdge"]')).not.toBeNull();
    });

    it('LastStep рендерит EmptyEdge когда активный шаг в середине (active=3)', () => {
      const { container } = render(<Stepper steps={7} active={3} />);

      const last = container.querySelector('[data-component="LastStepDiv"]')!;
      expect(last.querySelector('[data-component="EmptyEdge"]')).not.toBeNull();
    });
  });
});
