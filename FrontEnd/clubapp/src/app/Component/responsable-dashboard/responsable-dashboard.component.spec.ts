import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResponsableDashboardComponent } from './responsable-dashboard.component';
import { MagasinService } from '../Service/magasin.service';
import { of } from 'rxjs';

describe('ResponsableDashboardComponent', () => {
  let component: ResponsableDashboardComponent;
  let fixture: ComponentFixture<ResponsableDashboardComponent>;
  let magasinService: jasmine.SpyObj<MagasinService>;

  beforeEach(async () => {
    const magasinServiceSpy = jasmine.createSpyObj('MagasinService', [
      'getMagasins',
    ]);

    await TestBed.configureTestingModule({
      imports: [ResponsableDashboardComponent],
      providers: [{ provide: MagasinService, useValue: magasinServiceSpy }],
    }).compileComponents();

    magasinService = TestBed.inject(
      MagasinService
    ) as jasmine.SpyObj<MagasinService>;
    fixture = TestBed.createComponent(ResponsableDashboardComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load magasins on init', () => {
    const mockMagasins = [
      {
        id: 1,
        name: 'Test Store',
        description: 'Test',
        logo: '',
        address: '123 Rue',
      },
    ];
    magasinService.getMagasins.and.returnValue(of(mockMagasins));

    component.ngOnInit();

    expect(magasinService.getMagasins).toHaveBeenCalled();
    expect(component.magasins).toEqual(mockMagasins);
    expect(component.totalMagasins).toBe(1);
  });
});
