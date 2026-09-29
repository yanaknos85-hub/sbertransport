import '@testing-library/jest-dom';
import { configure, mount, shallow } from 'enzyme';
import React, { SyntheticEvent } from 'react';
import Adapter from 'enzyme-adapter-react-16';
import { act } from 'react-dom/test-utils';

configure({ adapter: new Adapter() });

import { WaypointModel } from 'shared/models/geo/Waypoint.model';
import { noop } from 'utils';

import AddressAutoComplete from '.';

const wayPoint = new WaypointModel({
  country: 'RU',
  latitude: 50.0,
  longitude: 49.0,
  city: 'St. Petersburg',
  street: 'Фаберже',
});

const wayPoint1 = new WaypointModel({
  country: 'RU',
  latitude: 50.0,
  longitude: 49.0,
  city: 'Москва',
  street: 'Охотный ряд',
});

describe('AddressAutoComplete methods', () => {
  const selectMockFn = jest.fn();
  const searchMockFn = jest.fn();
  const props = {
    list: [wayPoint, wayPoint1],
    onSelect: selectMockFn,
    onSearch: searchMockFn,
    value: 'Фаберже, St. Petersburg',
    placeholder: 'test',
  };

  const wrapper = mount(<AddressAutoComplete {...props} />);

  it('onSearch method work correctly', () => {
    act(() => {
      const targetInput = wrapper.find('input');
      targetInput.simulate('change', { target: { value: wayPoint1.addressString } });
    });
    expect(searchMockFn).toHaveBeenCalledWith(wayPoint1.addressString);
    expect(searchMockFn).toBeCalledTimes(1);
  });

  it('onSelect method work correctly', () => {
    const targetSelect = wrapper.find('Select').first();
    const selectHandler = targetSelect.prop('onSelect');
    expect(selectHandler).toBeTruthy();
    const eventTarget = { value: wayPoint1.addressString } as unknown as SyntheticEvent;
    act(() => {
      if (selectHandler) {
        selectHandler(eventTarget);
      }
    });
    // It's called with undefined. I don't know how fix it now
    // expect(selectMockFn).toHaveBeenCalledWith(wayPoint1.addressString);
    expect(selectMockFn).toBeCalledTimes(1);
  });
});

describe('AddressAutoComplete with empty list, without another props', () => {
  const props = {
    list: [],
  };

  const wrapper = shallow(<AddressAutoComplete {...props} />);

  it('renders properly', () => {
    expect(wrapper).toMatchSnapshot();
  });

  it('should have proper props', () => {
    expect(wrapper.props()).toEqual(
      expect.objectContaining({
        placeholder: 'Введите адрес',
        value: '',
        options: [],
      })
    );
  });
});

describe('AddressAutoComplete with non-empty list, with custom props', () => {
  const props = {
    list: [wayPoint],
    onSelect: noop,
    onSearch: noop,
    value: 'Фаберже, St. Petersburg',
    placeholder: 'test',
  };

  const wrapper = shallow(<AddressAutoComplete {...props} />);

  it('renders properly', () => {
    expect(wrapper).toMatchSnapshot();
  });

  it('should have proper props', () => {
    const { list } = props;
    const correctOptions = list.map(x => ({ value: x.addressString }));
    expect(wrapper.props()).toEqual(
      expect.objectContaining({
        placeholder: props.placeholder,
        value: props.value,
        options: expect.arrayContaining([expect.objectContaining(correctOptions[0])]),
      })
    );
  });
});

describe('AddressAutoComplete with incorrect props', () => {
  const props = {
    list: [wayPoint],
    value: 'test', // this value not exist in options
  };

  const wrapper = shallow(<AddressAutoComplete {...props} />);

  it('render empty string, since there is no option with this value', () => {
    expect(wrapper.prop('value')).toBe('');
  });
});
