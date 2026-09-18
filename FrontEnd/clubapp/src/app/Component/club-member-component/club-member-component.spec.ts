import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClubMemberComponent } from './club-member-component';

describe('ClubMemberComponent', () => {
  let component: ClubMemberComponent;
  let fixture: ComponentFixture<ClubMemberComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ClubMemberComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ClubMemberComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
