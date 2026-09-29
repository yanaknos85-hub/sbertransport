import React, { FC, useState } from 'react';
import { observer } from 'mobx-react';

import CargoMass from './CargoMassMultiple';
import CargoMassPreview from './CargoMassPreviewMultiple/CargoMassPreviewMultiple';
import CargoMassUpload from './CargoMassUploadMultiple/CargoMassUploadMultiple';

const SHOW_PREVIEW = false;
const SHOW_VIEW = true;

const CargoMassDemoMultiple: FC = observer(() => {
  const [showPreview, setShowPreview] = useState(SHOW_PREVIEW);
  const [showList, setShowList] = useState(SHOW_VIEW);

  const showUpload = !showPreview && !showList;

  return (
    <>
      {showUpload && (
        <CargoMassUpload
          style={{ marginBottom: '20px' }}
          onUpload={() => setShowPreview(true)}
        />
      )}

      {showPreview && (
        <CargoMassPreview
          onConfirm={() => {
            setShowList(true);
            setShowPreview(false);
          }}
          onClose={() => {
            setShowPreview(false);
          }}
        />
      )}
      {showList && <CargoMass />}
    </>
  );
});

export default CargoMassDemoMultiple;
