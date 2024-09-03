import Foundation
import Combine
import SwiftUI

class ProfileViewModel: ObservableObject {
  private let currencyRepository: CurrencyRepository
  private let profileRepository: ProfileRepository
  private let authRepository: AuthRepository
  private let userPreferences: UserPreferences
  var onSignOutCompleted: (() -> Void)? = nil
  
  @Published var messageToShow = ""
  @Published var shouldShowMessage = false
  
  @Published var pfpFromRemote: URL? = nil
  @Published var pfpToUpload = UIImage()
  @Published var isImagePickerUsed: Bool = false
  
  @Published var fullName: String = ""
  var currentEmail: String = "" // it changes only when confirmed by server
  @Published var email: String = ""
  @Published var countryCodeName: String? = nil
  @Published var personalData: PersonalData? = nil
  @Published var signOutResponse: SimpleResponse? = nil
  @Published var currencyRates: CurrencyRates? = nil
  
  private var cancellables = Set<AnyCancellable>()
  
  init(
    currencyRepository: CurrencyRepository,
    profileRepository: ProfileRepository,
    authRepository: AuthRepository,
    userPreferences: UserPreferences
  ) {
    self.currencyRepository = currencyRepository
    self.profileRepository = profileRepository
    self.authRepository = authRepository
    self.userPreferences = userPreferences
    // Automatically fetch data when initialized
    getPersonalData()
    getCurrency()
    $pfpToUpload.sink { image in
      if image != self.pfpToUpload {
        self.isImagePickerUsed = true
      }
    }.store(in: &cancellables)
  }
  
  // MARK: - Methods
  
  func setPfp(_ pfp: URL) {
    self.pfpFromRemote = pfp
  }
  
  func setFullName(_ value: String) {
    self.fullName = value
  }
  
  func setEmail(_ value: String) {
    self.email = value
  }
  
  func setCountryCodeName(_ value: String?) {
    self.countryCodeName = value
  }
  
  func getPersonalData() {
    profileRepository.personalDataPassThroughSubject
      .sink { completion in
        if case let .failure(error) = completion {
          self.showMessage(error.errorDescription)
        }
      } receiveValue: { resource in
        self.personalData = resource
        if let pfpUrl = resource.pfpUrl {
          self.pfpFromRemote = URL(string: pfpUrl)
        }
        self.fullName = resource.fullName
        self.currentEmail = resource.email
        self.email = resource.email
        self.countryCodeName = resource.country
      }
      .store(in: &cancellables)
    
    profileRepository.getPersonalData()
  }
  
  func save() {
    if(!fullName.isEmpty && ((countryCodeName?.isEmpty) != nil) && !email.isEmpty) {
      profileRepository.updateProfile(
        fullName: fullName,
        country: countryCodeName!,
        // We shouldn't send email field if there's no change
        email: email == currentEmail ? nil : email,
        pfpUrl: pfpToUpload
      )
      .sink { completion in
        if case let .failure(error) = completion {
          self.showMessage(error.errorDescription)
        }
      } receiveValue: { resource in
          self.updatePersonalDataInMemory(personalData: resource)
          self.showMessage(L("saved"))
      }
      .store(in: &cancellables)
    } else {
      self.showMessage(L("please_fill_all_fields"))
    }
  }
  
  private func updatePersonalDataInMemory(personalData: PersonalData) {
    self.fullName = personalData.fullName
    self.email = personalData.email
    self.countryCodeName = personalData.country
  }
  
  func getCurrency() {
    currencyRepository.currencyPassThroughSubject
      .sink { completion in
        if case let .failure(error) = completion {
          self.showMessage(error.errorDescription)
        }
      } receiveValue: { resource in
        self.currencyRates = resource
      }
      .store(in: &cancellables)
    
    currencyRepository.getCurrency()
  }
  
  func signOut() {
    authRepository.signOut()
      .sink { completion in
        if case let .failure(error) = completion {
          self.showMessage(error.errorDescription)
        }
      } receiveValue: { response in
        self.signOutResponse = response
        self.userPreferences.setToken(value: nil)
        self.onSignOutCompleted?()
        self.showMessage(response.message)
      }
      .store(in: &cancellables)
  }
  
  func showMessage(_ message: String) {
    messageToShow = message
    shouldShowMessage = true
  }
}
