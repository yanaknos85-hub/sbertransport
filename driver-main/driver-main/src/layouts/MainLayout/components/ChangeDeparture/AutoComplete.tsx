/* eslint-disable  @typescript-eslint/no-explicit-any */
/* eslint-disable jsx-a11y/click-events-have-key-events, jsx-a11y/no-noninteractive-element-interactions */
/* eslint-disable jsx-a11y/no-autofocus */
import { useState, useCallback } from 'react';
import './styles.css';
import debounce from 'lodash/debounce';
import { useGeoService } from 'api/services/Geo/Geo';
import { TWaypoint } from 'api/services/Geo/Geo.types';
import { TextArea } from 'antd-mobile';

interface Props {
  handleDepartureChange: React.Dispatch<React.SetStateAction<any>>;
}

const arrToString = (arr: (string | undefined)[]) => arr.filter(Boolean).join(', ');

const Autocomplete = ({ handleDepartureChange }: Props) => {
  const { getCoordinateByAddress } = useGeoService();
  const [inputValue, setInputValue] = useState('');
  const [suggestions, setSuggestions] = useState<TWaypoint[]>([]);

  const debounceSearch = useCallback(
    debounce(value => {
      if (value.length > 3) {
        getCoordinateByAddress(value).then(resp => {
          setSuggestions(resp);
        });
      } else {
        setSuggestions([]);
      }
    }, 1000),
    []
  );

  const handleChange = value => {
    setInputValue(value);
    debounceSearch(value);
  };

  const handleSelectSuggestion = item => {
    setInputValue(arrToString([
      item.street,
      item.house,
      item.place,
      item.livingArea,
      item.settlement,
      item.city !== item.region ? item.city : '',
      item.district,
      item.region,
      item.country,
    ]));
    handleDepartureChange([item.latitude, item.longitude]);
    setSuggestions([]);
  };

  return (
    <div className="autocomplete">
      <TextArea
        name="text"
        value={inputValue}
        onChange={handleChange}
        placeholder="Введите адрес"
        autoFocus
      />
      {!!suggestions.length && (
      <ul className="suggestion-list">
        {suggestions.map((item, index) => (
          <li
            key={index}
            onClick={() => handleSelectSuggestion(item)}
            className="suggestion-item"
          >
            {arrToString([
              item.street,
              item.house,
              item.place,
              item.livingArea,
              item.settlement,
              item.city !== item.region ? item.city : '',
              item.district,
              item.region,
              item.country,
            ])}
          </li>
        ))}
      </ul>
      )}
    </div>
  );
};

export default Autocomplete;
