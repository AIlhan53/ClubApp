import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InviationStatusComponent } from './inviation-status-component';

describe('InviationStatusComponent', () => {
  let component: InviationStatusComponent;
  let fixture: ComponentFixture<InviationStatusComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InviationStatusComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(InviationStatusComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
